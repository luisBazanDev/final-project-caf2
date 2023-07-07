import { DataTypes, Model } from "sequelize";

class Log extends Model {}

export default Log;
export function register(sequelize) {
  Log.init(
    {
      author: {
        type: DataTypes.STRING,
        allowNull: false,
      },
      latitude: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },
      longitude: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },
      rssi: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },
      snr: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },
      payload: {
        type: DataTypes.STRING,
        allowNull: false,
      },
      frecuency: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },
      spreading_factor: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },
      bandwidth: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },
      code_rate: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },
      preamble_length: {
        type: DataTypes.INTEGER,
        allowNull: false,
      },
      tx_power: {
        type: DataTypes.INTEGER,
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
