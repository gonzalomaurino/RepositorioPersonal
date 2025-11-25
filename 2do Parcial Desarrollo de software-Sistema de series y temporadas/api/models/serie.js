// modelo Serie.js
import { DataTypes, Model } from "sequelize";
import sequelize from "../db.js";

class Serie extends Model {}

Serie.init(
  {
    id: {
      type: DataTypes.INTEGER,
      primaryKey: true,
      autoIncrement: true,
      field: "id"
    },
    titulo: {
      type: DataTypes.TEXT,
      allowNull: false,
      field: "titulo"
    },
    plataforma: {
      type: DataTypes.TEXT,
      allowNull: false,
      field: "plataforma"
    }
  },
  {
    sequelize,
    modelName: "Serie",
    tableName: "Series",
    timestamps: false
  }
);

export default Serie;