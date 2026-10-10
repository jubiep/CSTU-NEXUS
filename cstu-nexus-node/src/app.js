const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8080;

// Middlewares
app.use(cors());
app.use(express.json());

// Health Check Endpoint สำหรับทดสอบ
app.get('/health', (req, res) => {
    res.status(200).json({
        service: 'CSTU NEXUS API',
        status: 'UP',
        runtime: 'Node.js'
    });
});

// เริ่มต้นเปิด Server
app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});