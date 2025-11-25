import express from "express";
import serieService from "../services/serieService.js";

const router = express.Router();

router.get("/", async (req, res) => {
  try {
    const series = await serieService.obtenerTodas();
    res.json(series);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

export default router;
