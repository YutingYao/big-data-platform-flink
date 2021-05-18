const mongoose = require('mongoose')

const turtleMetaSchema = new mongoose.Schema({
  topic: String,
  partition: Number,
  offset: Number,
})

turtleMetaSchema.set('toJSON', {
  transform: (document, returnedObject) => {
    returnedObject.id = returnedObject._id.toString()
    delete returnedObject._id
    delete returnedObject.__v
  }
})

const TurtleMeta = mongoose.model('TurtleMeta', turtleMetaSchema)

module.exports = TurtleMeta
