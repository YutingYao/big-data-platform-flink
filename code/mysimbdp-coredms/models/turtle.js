const mongoose = require('mongoose')

const turtleSchema = new mongoose.Schema({
  topic: String,
  message: {
    turtleAccelReport: {
      dev_id: String,
      acceleration: Number,
    },
  },
})

turtleSchema.set('toJSON', {
  transform: (document, returnedObject) => {
    returnedObject.id = returnedObject._id.toString()
    returnedObject.meta_id = returnedObject.metadata_id.toString()
    delete returnedObject._id
    delete returnedObject.metadata_id
    delete returnedObject.__v
  }
})

const Turtle = mongoose.model('Turtle', turtleSchema)

module.exports = Turtle
