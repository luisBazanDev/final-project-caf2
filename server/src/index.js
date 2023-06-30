import { config } from "dotenv";
config();
import express from "express";
import { Sequelize } from "sequelize";

// Configs
const orm = new Sequelize(process.env.MYSQL_USER);
const app = express();
const PORT = process.env.BACKEND_PORT;

app.listen(PORT, () => {
  console.log(`Server on port: ${PORT}`);
});
