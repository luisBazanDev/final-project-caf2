import { Router } from "express";
import Log from "../models/Log.js";

const router = Router();

router.get("/all", async (req, res) => {
  res.json(await Log.findAll());
});

router.post("/create", async (req, res) => {
  console.log(req.body);
  const msg = req.body.msg ?? "no message";

  await Log.create({ msg });

  res.send("OK");
});

export default router;
