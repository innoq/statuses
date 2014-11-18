#!/usr/bin/env ruby
puts "Compiling & creating main jar."
if system('lein uberjar')
  mainjar = Dir['target/statuses-*-standalone.jar'].last
  mainjar['target/'] = '' # and I actually considered this language beautiful once?
  puts "Creating run script for #{mainjar}."
  f = File.new('./run.sh', 'w+')
  f.chmod(0744)
  f.puts "export LANG=en_US.utf8\njava -jar #{mainjar} $*"
  f.close
  puts "Creating log file"
  log = `git log -1 --pretty="%H"`
  f = File.new('./headrev.txt', 'w+')
  f.chmod(0744)
  f.puts "#{log}"
  f.close
  puts "Synchronizing with remote dir."
  `rsync --exclude data --delete -avz run.sh target/#{mainjar} headrev.txt public statuses@internal2.innoq.com:/home/statuses`
  puts "Done."
end

