require 'puma'
require 'sinatra/base'

puts 'I am the greeter!'

class Greeter < Sinatra::Base
  set :server, :puma

  get '/' do
    'Hello World, I am at your service!'
  end
end
