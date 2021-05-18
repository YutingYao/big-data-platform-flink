const logger = require('./logger')
const morgan = require('morgan')

/* morgan.token('file', request => {
  if (request.method === 'POST') {
    return JSON.stringify(request.files.file.name)
  }
}) */

const unknownEndpoint = (_request, response) => {
  response.status(404).send({ error: 'unknown endpoint' })
}

const errorHandler = (error, _request, response, next) => {
  if (error.name === 'CastError') {
    return response.status(400).send({ error: 'malformatted id' })
  } else if (error.name === 'ValidationError') {
    return response.status(400).json({ error: error.message })
  } else if (error.name === 'JsonWebTokenError') {
    return response.status(401).json({ error: 'invalid token' })
  }
  logger.error(error.message)
  next(error)
}

module.exports = {
  morgan,
  unknownEndpoint,
  errorHandler
}
