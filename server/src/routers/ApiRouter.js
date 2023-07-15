import { Router } from "express";
import Log from "../models/Log.js";

const router = Router();
var temperatureHumidity = {
  temperature: 20,
  humidity: 20,
};

router.get("/all", async (req, res) => {
  res.json(await Log.findAll());
});

router.get("/temp", async (req, res) => {
  res.json(temperatureHumidity);
});

router.post("/temp", async (req, res) => {
  console.log(req.body.data);
  if (req.body.data) temperatureHumidity = req.body.data;
});

router.post("/create", async (req, res) => {
  const data = req.body.data;

  console.log(`new req from ${req.ip}:`);
  console.table(data);

  if (!data) return res.send("BAD REQUEST");

  const localUuid = data.uuid;

  await Log.create({
    author: data.author,
    latitude: data.latitude,
    longitude: data.longitude,
    altitude: data.altitude,
    rssi: data.rssi,
    snr: data.snr,
    payload: data.payload,
    frecuency: data.frecuency,
    spreading_factor: data.spreading_factor,
    bandwidth: data.bandwidth,
    code_rate: data.code_rate,
    preamble_length: data.preamble_length,
    tx_power: data.tx_power,
  });

  res.json({
    uuid: localUuid,
  });
});

router.post("/ping", async (req, res) => {
  console.log(`Ping from: ${req.ip}`);
  res.send("pong!");
});

export default router;
