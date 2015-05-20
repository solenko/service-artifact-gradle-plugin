require 'puma'
require 'sinatra/base'

puts 'I am the greeter!'

class Greeter < Sinatra::Base
  set :server, :webrick

  get '/' do
    'Hello World, I am at your service!'
  end
end
