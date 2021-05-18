const turtlesRouter = require('express').Router()
const Turtle = require('../models/turtle')
const TurtleMeta = require('../models/turtleMeta')

turtlesRouter.get('/', async (req, res) => {
  const turtles = await Turtle.find({})
  res.json(turtles)
})

turtlesRouter.post('/', async (req, res) => {
  // MongoDB write operations are atomic at the level of a single document
  try {
    const onInsert = (err, newTurtles) => {
      if (err) {
        console.log(err)
        res.status(400).json(err)
      } else {
        res.status(201).json(newTurtles)
      }
    }
    const jsonObject = req.body
    Turtle.collection.insertOne(
      jsonObject,
      {
        writeConcern: { w: 'majority' }
      },
      onInsert
    )
  } catch (err) {
    console.log('Unknown error', err)
    res.status(500).send(err)
  }
})

turtlesRouter.post('/meta', async (req, res) => {
  // MongoDB write operations are atomic at the level of a single document
  try {
    const onInsert = (err, newTurtlesMeta) => {
      if (err) {
        console.log(err)
        res.status(400).json(err)
      } else {
        res.status(201).json(newTurtlesMeta)
      }
    }
    const jsonObject = req.body
    console.log('turtlesRouter/meta received:', jsonObject)
    TurtleMeta.collection.insertOne(
      jsonObject,
      {
        writeConcern: { w: 'majority' }
      },
      onInsert
    )
  } catch (err) {
    console.log('Unknown error', err)
    res.status(500).send(err)
  }
})

module.exports = turtlesRouter
