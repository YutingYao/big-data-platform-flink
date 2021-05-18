const config = require('./utils/config')
const express = require('express')
require('express-async-errors')
const fileUpload = require('express-fileupload')
const cors = require('cors')
const bodyParser = require('body-parser')
const turtlesRouter = require('./controllers/turtles')
const middleware = require('./utils/middleware')
const logger = require('./utils/logger')
const mongoose = require('mongoose')

const app = express()

mongoose.connect(config.MONGODB_URI_TENANT_A, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  useFindAndModify: false,
  useCreateIndex: true,
  dbName: 'tenant-a-db',
})
  .then(() => {
    logger.info('connected to MongoDB')
  })
  .catch(e => {
    logger.error('error connection to MongoDB:', e.message)
  })

app.use(fileUpload({ createParentPath: true }))
app.use(cors()) // Allow access from any domain
app.use(express.static('build'))
app.use(bodyParser.json())
app.use(bodyParser.urlencoded({ extended: true }))
app.use(middleware.morgan(
  ':method :url :status - :response-time ms'
))

app.use('/api/turtles', turtlesRouter)

app.use(middleware.unknownEndpoint)
app.use(middleware.errorHandler)

module.exports = app
