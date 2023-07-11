import { DataTypes, Model } from "sequelize";

class Log extends Model {}

export default Log;
export function register(sequelize) {
  Log.init(
    {
      author: {
        type: DataTypes.STRING,
        allowNull: true,
      },
      latitude: {
        type: DataTypes.DOUBLE,
        allowNull: true,
      },
      longitude: {
        type: DataTypes.DOUBLE,
        allowNull: true,
      },
      altitude: {
        type: DataTypes.DOUBLE,
        allowNull: true,
      },
      rssi: {
        type: DataTypes.INTEGER,
        allowNull: true,
      },
      snr: {
        type: DataTypes.INTEGER,
        allowNull: true,
      },
      payload: {
        type: DataTypes.STRING,
        allowNull: true,
      },
      frecuency: {
        type: DataTypes.INTEGER,
        allowNull: true,
      },
      spreading_factor: {
        type: DataTypes.INTEGER,
        allowNull: true,
      },
      bandwidth: {
        type: DataTypes.INTEGER,
        allowNull: true,
      },
      code_rate: {
        type: DataTypes.INTEGER,
        allowNull: true,
      },
      preamble_length: {
        type: DataTypes.INTEGER,
        allowNull: true,
      },
      tx_power: {
        type: DataTypes.INTEGER,
        allowNull: true,
      },
    },
    {
      sequelize,
      modelName: "Log",
    }
  );
  (async () => {
    await sequelize.sync({ force: true });
  })();
}
