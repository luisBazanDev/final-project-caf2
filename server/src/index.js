import express from "express";
import { config } from "dotenv";
config();
import ApiRouter from "./routers/ApiRouter.js";
import { start as startDB } from "./DatabaseManager.js";

(async () => {
  // Configs
  await startDB();

  const app = express();
  app.use(express.json());
  app.use(express.static("server/public"));
  const PORT = process.env.BACKEND_PORT;
  app.use("/api", ApiRouter);

  app.listen(PORT, () => {
    console.log(`Server on port: ${PORT}`);
  });
})();
