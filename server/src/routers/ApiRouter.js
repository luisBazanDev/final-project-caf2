import { Router } from "express";
import Log from "../models/Log.js";

const router = Router();

router.get("/all", async (req, res) => {
  res.json(await Log.findAll());
});

router.post("/create", async (req, res) => {
  const msg = req.body.msg;
  const author = req.body.author;

  if (!msg || !author) return res.send("BAD REQUEST");

  await Log.create({ msg });

  res.send("OK");
});

router.post("/ping", async (req, res) => {
  console.log(`Ping from: ${req.ip}`);
  res.send("pong!");
});

export default router;
