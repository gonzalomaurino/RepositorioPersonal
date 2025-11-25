import express from "express";
import temporadaService from "../services/temporadaService.js";

const router = express.Router();

// Obtener todas las temporadas

router.get("/", async (req, res) => {
    try {
        const { pagina, limite } = req.query;
        const temporadas = await temporadaService.obtenerTodos({ pagina, limite });
        res.json(temporadas);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

//Filtrado del front
router.get('/filtrar', async (req, res) => {
    const { tituloSerie, plataforma, genero, estrellas, soloOriginales } = req.query;
    try {
        const resultado = await temporadaService.buscarFiltrado({
            tituloSerie,
            plataforma,
            genero,
            estrellas,
            soloOriginales
        });
        res.json(resultado);
    } catch (error) {
        console.error("Error al filtrar temporadas:", error);
        res.status(500).json({ error: "Error interno al filtrar temporadas" });
    }
});

// Obtener temporada por ID

router.get("/:id", async (req, res) => {
    try {
        const temporada = await temporadaService.obtenerPorId(parseInt(req.params.id));
        if (!temporada) return res.status(404).json({ error: "Temporada no encontrada." });
        res.json(temporada);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Crear temporada

router.post("/", async (req, res) => {
    try {
        const creada = await temporadaService.crear(req.body);
        res.status(201).json(creada);
    } catch (err) {
        res.status(400).json({ error: err.message });
    }
});

// Actualizar temporada

router.put("/:id", async (req, res) => {
    try {
        const actualizada = await temporadaService.actualizar(parseInt(req.params.id), req.body);
        res.json(actualizada);
    } catch (err) {
        res.status(400).json({ error: err.message });
    }
});

// Eliminar temporada

router.delete("/:id", async (req, res) => {
    try {
        await temporadaService.eliminar(parseInt(req.params.id));
        res.status(204).end();
    } catch (err) {
        if (err.message.includes("no encontrada")) {
            res.status(404).json({ error: err.message });
        } else {
            res.status(500).json({ error: "Error inesperado." });
        }
    }
});

// Contar todas las temporadas --> NO LO PIDE PRE ENUCNIADO, BORRAR SINO
router.get("/contar/todas", async (req, res) => {
    try {
        const total = await temporadaService.contarTodos();
        res.json({ total });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});



export default router;
