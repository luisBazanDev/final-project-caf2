import express from "express";
import https from "https";
import { config } from "dotenv";
config();
import ApiRouter from "./routers/ApiRouter.js";
import { start as startDB } from "./DatabaseManager.js";
import { readFileSync } from "fs";

(async () => {
  // Configs
  await startDB();

  const app = express();
  app.use(express.json());
  const PORT = process.env.BACKEND_PORT;
  app.use("/api", ApiRouter);

  https
    .createServer(
      {
        key: readFileSync("./server/https-keys/ca-key.pem"),
        cert: readFileSync("./server/https-keys/ca-cert.pem"),
      },
      app
    )
    .listen(PORT, () => {
      console.log(`Server on port: ${PORT}`);
    });
})();
