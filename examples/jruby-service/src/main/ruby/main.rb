$LOAD_PATH.unshift(File.dirname(__FILE__))
require 'app/greeter'

puts "Hello world!"

Greeter.run!
