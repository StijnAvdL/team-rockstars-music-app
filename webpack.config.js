const webpack = require("webpack");
const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const HtmlWebPackPlugin = require("html-webpack-plugin");
// const dotenv = require("dotenv-webpack");

var argv = require("minimist")(process.argv.slice(2));
const isWeb = argv && argv.target === "web";
const envFile = argv && argv.env && argv.env.STAGE ? argv.env.STAGE : "staging";
const output = isWeb ? "/dist" : "/cordova/www";

module.exports = {
  entry: ["react-hot-loader/patch", "./src/index.jsx"],
  output: {
    path: __dirname + output,
    filename: "bundle.js",
    publicPath: "/"
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: ["babel-loader"]
      },
      {
        test: /\.(css|less|scss)$/,
        exclude: /node_modules/,
        use: ["style-loader", "css-loader"]
      },
      {
        test: /\.(png|jpe?g|gif|svg)$/i,
        loader: "file-loader",
        options: {
          name: "[name].[contenthash].[ext]",
          outputPath: "images/",
          publicPath: "images/"
        }
      }
    ]
  },
  resolve: {
    extensions: ["*", ".js", ".jsx"],
    symlinks: false
  },
  externals: /^images/i,
  plugins: [
    new CleanWebpackPlugin({
      cleanOnceBeforeBuildPatterns: ['**/*']
    }),
    new HtmlWebPackPlugin({ template: "./src/index.html" }),
    new webpack.HotModuleReplacementPlugin(),
    // new dotenv({ path: "./.env-" + envFile })
  ],
  devServer: {
    historyApiFallback: true,
    contentBase: "./dist",
    hot: true,
    host: "0.0.0.0"
  }
};
