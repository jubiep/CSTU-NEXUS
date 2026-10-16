
param(
    [string]$OutputPath = "database/seed.sql"
)

$ErrorActionPreference = "Stop"

function Read-CsvFile {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "CSV file not found: $Path"
    }

    $rows = @(Import-Csv -LiteralPath $Path -Encoding UTF8)

    if ($rows.Count -eq 0) {
        throw "CSV file is empty or has no header: $Path"
    }

    return $rows
}

function Assert-Headers {
    param(
        [object[]]$Rows,
        [string[]]$RequiredHeaders,
        [string]$FileName
    )

    $actualHeaders = @($Rows[0].PSObject.Properties.Name)

    foreach ($header in $RequiredHeaders) {
        if ($actualHeaders -notcontains $header) {
            throw "Missing required column '$header' in $FileName"
        }
    }
}

function Assert-Unique {
    param(
        [object[]]$Rows,
        [string]$PropertyName,
        [string]$Label
    )

    $seen = @{}

    foreach ($row in $Rows) {
        $value = [string]$row.$PropertyName

        if ([string]::IsNullOrWhiteSpace($value)) {
            throw "$Label cannot be empty."
        }

        if ($seen.ContainsKey($value)) {
            throw "Duplicate $Label found: $value"
        }

        $seen[$value] = $true
    }
}

function ConvertTo-SqlString {
    param([AllowEmptyString()][string]$Value)

    $escaped = $Value.Replace('\', '\\')
    $escaped = $escaped.Replace("'", "''")
    $escaped = $escaped.Replace("`r", '\r')
    $escaped = $escaped.Replace("`n", '\n')
    $escaped = $escaped.Replace("`t", '\t')

    return "E'$escaped'"
}

function Get-RequiredValue {
    param(
        [object]$Row,
        [string]$PropertyName,
        [string]$Label
    )

    $value = [string]$Row.$PropertyName

    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "$Label cannot be empty."
    }

    return $value
}

$coursesPath = "database/courses.csv"
$sectionsPath = "database/sections.csv"

$courses = @(Read-CsvFile -Path $coursesPath)
$sections = @(Read-CsvFile -Path $sectionsPath)

Assert-Headers -Rows $courses `
    -RequiredHeaders @("course_id", "course_name", "recommended_year") `
    -FileName $coursesPath

Assert-Headers -Rows $sections `
    -RequiredHeaders @("course_id", "section_id", "section_number", "instructor_name") `
    -FileName $sectionsPath

Assert-Unique -Rows $courses `
    -PropertyName "course_id" `
    -Label "course_id"

Assert-Unique -Rows $sections `
    -PropertyName "section_id" `
    -Label "section_id"

$courseIds = @{}

foreach ($course in $courses) {
    $courseId = Get-RequiredValue $course "course_id" "course_id"
    $courseName = Get-RequiredValue $course "course_name" "course_name"
    $yearText = Get-RequiredValue $course "recommended_year" "recommended_year"

    $year = 0
    $validYear = [int]::TryParse(
        $yearText,
        [ref]$year
    )

    if (-not $validYear -or $year -lt 1 -or $year -gt 4) {
        throw "Invalid recommended_year for course '$courseId': $yearText"
    }

    $courseIds[$courseId] = $true
}

foreach ($section in $sections) {
    $courseId = Get-RequiredValue $section "course_id" "section course_id"
    $sectionId = Get-RequiredValue $section "section_id" "section_id"
    $sectionNumber = Get-RequiredValue $section "section_number" "section_number"

    if (-not $courseIds.ContainsKey($courseId)) {
        throw "Section '$sectionId' references unknown course '$courseId'."
    }
}

$expectedPlaceholderIds = @(
    "el105_000000",
    "el295_000000",
    "el395_000000"
)

$placeholderRows = @(
    $sections | Where-Object { $_.section_number -eq "000000" }
)

if ($placeholderRows.Count -ne 3) {
    throw "Expected exactly 3 placeholder sections with section_number 000000; found $($placeholderRows.Count)."
}

foreach ($id in $expectedPlaceholderIds) {
    $matches = @(
        $placeholderRows | Where-Object { $_.section_id -eq $id }
    )

    if ($matches.Count -ne 1) {
        throw "Expected exactly one placeholder section with section_id '$id'."
    }
}

$sections = @(
    $sections | Where-Object { $_.section_number -ne "000000" }
)

foreach ($section in $sections) {
    if ($section.section_number.Length -gt 10) {
        throw "section_number exceeds 10 characters: $($section.section_id)"
    }
}

if ($courses.Count -ne 38) {
    throw "Expected 38 courses, found $($courses.Count)."
}

if ($sections.Count -ne 63) {
    throw "Expected 63 valid sections, found $($sections.Count)."
}


$sql = [System.Collections.Generic.List[string]]::new()

$sql.Add("-- Generated from courses.csv and sections.csv")
$sql.Add("-- Placeholder sections with section_number 000000 are excluded.")
$sql.Add("")
$sql.Add("BEGIN;")
$sql.Add("")
$sql.Add("INSERT INTO courses (course_id, course_name, recommended_year)")
$sql.Add("VALUES")

for ($i = 0; $i -lt $courses.Count; $i++) {
    $course = $courses[$i]

    $courseId = ConvertTo-SqlString ([string]$course.course_id)
    $courseName = ConvertTo-SqlString ([string]$course.course_name)
    $year = [int]$course.recommended_year

    $suffix = if ($i -lt $courses.Count - 1) { "," } else { ";" }

    $sql.Add("($courseId, $courseName, $year)$suffix")
}

$sql.Add("")
$sql.Add("INSERT INTO sections (course_id, section_id, section_number, instructor_name)")
$sql.Add("VALUES")

for ($i = 0; $i -lt $sections.Count; $i++) {
    $section = $sections[$i]

    $courseId = ConvertTo-SqlString ([string]$section.course_id)
    $sectionId = ConvertTo-SqlString ([string]$section.section_id)
    $sectionNumber = ConvertTo-SqlString ([string]$section.section_number)

    if ([string]::IsNullOrWhiteSpace([string]$section.instructor_name)) {
        $instructor = "NULL"
    }
    else {
        $instructor = ConvertTo-SqlString ([string]$section.instructor_name)
    }

    $suffix = if ($i -lt $sections.Count - 1) { "," } else { ";" }

    $sql.Add("($courseId, $sectionId, $sectionNumber, $instructor)$suffix")
}

$sql.Add("")
$sql.Add("COMMIT;")

$outputDirectory = Split-Path -Parent $OutputPath

if ($outputDirectory -and -not (Test-Path -LiteralPath $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
}

$outputContent = ($sql -join [Environment]::NewLine) + [Environment]::NewLine
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

[System.IO.File]::WriteAllText(
    $OutputPath,
    $outputContent,
    $utf8NoBom
)

Write-Host "Seed SQL generated successfully."
Write-Host "Courses: $($courses.Count)"
Write-Host "Sections: $($sections.Count)"
Write-Host "Output: $OutputPath"
