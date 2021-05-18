require('dotenv').config()

let PORT = process.env.PORT
let MONGODB_URI_TENANT_A = process.env.MONGODB_URI_TENANT_A

module.exports = {
  MONGODB_URI_TENANT_A,
  PORT
}
