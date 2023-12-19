const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const VueLoaderPlugin = require('vue-loader/lib/plugin');
const config = require('./config');
const Stylish = require('webpack-stylish');
const FriendlyErrorsWebpackPlugin = require('friendly-errors-webpack-plugin');
const Jarvis = require('webpack-jarvis');
const path = require('path');

const NODE_ENV = process.env.NODE_ENV;
const HOST = process.env.HOST;
const PORT = process.env.PORT && Number(process.env.PORT);

const FILE_PATH = NODE_ENV === 'production' ? ['/js/', '/img/', '/css/'] : ['', '', ''];

/**
 * javascript 코드를 빌드하기 위한 webpack4 세팅.
 *
 * @type {{mode: string, devtool: string, devServer: {port: string | number, host: string | string,
 * clientLogLevel: string, contentBase: string}, output: {path: string, filename: string, publicPath: string},
 * entry: {"h-with": string}, resolve: {extensions: string[],
 * alias: {vue: string}, modules: string[]}, optimization: {}, plugins: *[], module: {rules: *[]}}}
 */

module.exports = {
  // webpack4 mode : development or production
  mode: NODE_ENV,
  entry: {
    [FILE_PATH[0] + 'datacore']: './src/main.js'
  },
  devtool: 'inline-source-map',
  devServer: {
    clientLogLevel: 'warning',
    contentBase: './dist',
    // compress: true,
    host: HOST || config.dev.host,
    port: PORT || config.dev.port,
    historyApiFallback: true,
    proxy: {
      '/': {
        target: 'http://localhost:8083' // api server (development mode)
      }
    }
  },
  // result config
  output: {
    path: __dirname + '../../src/main/resources/static',
    filename: '[name].js'
  },
  module: {
    rules: [
      {
        test: /\.vue?$/,
        loader: 'vue-loader',
        options: {
          extractCSS: true
        }
      }, {
        test: /.s[a|c]ss$/,
        use: ['vue-style-loader', 'css-loader', 'sass-loader']
      }, {
        test: /\.css$/,
        use: ['vue-style-loader', MiniCssExtractPlugin.loader, 'css-loader'],
      }, {
        test: /\.(ico|png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        loader: 'url-loader',
        options: {
          name: FILE_PATH[1] + '[hash].[ext]',
          limit: 10000
        }
      }, {
        test: '/.js$/',
        loader: 'babel-loader',
        exclude: ['/node_modules']
      }
    ]
  },
  plugins: [
    new HtmlWebpackPlugin({
      title: 'Datacore-ui',
      template: './public/index.html',
      hash: true,
      filename: 'index.html',
      excludeChunks: ['multiple']
    }),
    new MiniCssExtractPlugin({ filename: FILE_PATH[2] + 'datacore.css' }),
    new VueLoaderPlugin(),
    new Stylish(),
    new FriendlyErrorsWebpackPlugin(),
    new Jarvis({
      port: 1337
    })
  ],
  optimization: {},
  resolve: {
    alias: {
      vue: 'vue/dist/vue.js',
      '@': path.resolve(__dirname, '../frontend/src/')
    },
    modules: ['node_modules'],
    extensions: ['.js', '.json', '.vue', '.css']
  }
};
