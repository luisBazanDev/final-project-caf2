import { DataTypes, Model } from "sequelize";

class Log extends Model {}

export default Log;
export function register(sequelize) {
  Log.init(
    {
      msg: {
        type: DataTypes.STRING,
        allowNull: false,
      },
      author: {
        type: DataTypes.STRING,
        allowNull: false,
      },
    },
    {
      sequelize,
      modelName: "Log",
    }
  );
  (async () => {
    await sequelize.sync({ force: false });
  })();
}
