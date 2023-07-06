import express from "express";
import https from "https";
import { config } from "dotenv";
config();
import ApiRouter from "./routers/ApiRouter.js";
import { start as startDB } from "./DatabaseManager.js";
import { fsync } from "fs";

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
        key: fsync("server/https-keys/key.pem"),
        cert: fsync("server/https-keys/cert.pem"),
      },
      app
    )
    .listen(PORT, () => {
      console.log(`Server on port: ${PORT}`);
    });
})();
