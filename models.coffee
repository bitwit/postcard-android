mongoose = require 'mongoose'
Schema = mongoose.Schema
mongoose.connect 'mongodb://localhost/postcardapp'

models = {}

models.Types = {
  ObjectId : mongoose.Types.ObjectId
}

###
#  User Model
###
attributes =
  username: String
  password: String
  createdAt: {"type": Date, "default": Date.now}
  lastLoginAt: {"type": Date, "default": Date.now}
schema = new Schema attributes, strict: yes
models.User = mongoose.model 'User', schema

###
#  Account Model
###
attributes =
  network: String
  token: String
  user: Schema.Types.ObjectId
schema = new Schema attributes, strict: yes
models.Activity = mongoose.model 'Account', schema

###
# Activity Model
###
attributes =
  user: Schema.Types.ObjectId
  message: String
  date: {"type": Date, "default": Date.now}
  accounts: [{"type": Schema.Types.ObjectId, "ref": "Account"}]
schema = new Schema attributes, strict: yes
models.Activity = mongoose.model 'Activity', schema

module.exports = models