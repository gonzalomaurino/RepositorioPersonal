// db.js
import { Sequelize } from "sequelize";


const sequelize = new Sequelize({
    dialect: "sqlite",
    storage: "./data/dbSeries.sqlite"
});

export default sequelize;
