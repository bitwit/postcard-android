'use strict'

express = require 'express'
path = require 'path'

models = require './models'

routes = require './routes'

api =
  activities: require './webapp/routes/api/activities'
  networks: require './webapp/routes/api/networks'
  users: require './webapp/routes/api/users'


# Set default node environment to development
port = 9898
process.env.NODE_ENV = process.env.NODE_ENV || 'development';

# Application Config
##config = require('./lib/config/config');

app = express();
app.set 'views', __dirname + '/views'
app.set 'view engine', 'jade'
app.use express.favicon()
app.use express.logger('dev')
app.use express.bodyParser()
app.use express.methodOverride()
app.use app.router
app.use express.static(path.join(__dirname, 'public'))

# Express settings
#require('./lib/config/express')(app);

# Routing
#require('./lib/routes')(app);

# View routes
app.get '/', routes.index
app.get '/template/:template', routes.template

app.get '/api/networks', api.networks.index

app.get '/api/users/account', api.users.account

app.post '/api/activities/new', api.activities.new
app.post '/api/activities/edit/:activityId', api.activities.edit
app.post '/api/activities/delete/:activityId', api.activities.delete

# Start server
app.listen port, ->
  console.log 'Express server listening on port %d in %s mode', port, app.get('env')

# Expose app
exports = module.exports = app;
