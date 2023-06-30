import { Sequelize } from "sequelize";
import { register as LogRegister } from "./models/Log.js";
var sequelize = null;
export async function start() {
  sequelize = new Sequelize(
    process.env.MYSQL_DB,
    process.env.MYSQL_USER,
    process.env.MYSQL_PASSWORD,
    {
      host: process.env.MYSQL_HOST,
      port: process.env.MYSQL_PORT,
      dialect: "mysql",
    }
  );

  try {
    await sequelize.authenticate();
    console.log("Connection has been established successfully.");
    await LogRegister(sequelize);
  } catch (error) {
    console.error("Unable to connect to the database:", error);
  }
}

export default sequelize;
