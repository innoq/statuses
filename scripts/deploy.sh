#!/usr/bin/env ruby
puts "Compiling & creating main jar."
if system('lein jar')
  mainjar = Dir['statuses-*.jar'].last
  puts "Creating run script for #{mainjar}."
  f = File.new('./run.sh', 'w+')
  f.puts "java -classpath #{mainjar}:#{Dir['./lib/*'].join(':')} statuses.server"
  f.close
  puts "Synchronizing with remote dir."
  `rsync --delete -avz run.sh #{mainjar} lib internal2.innoq.com:/home/statuses`
  puts "Done."
end

