Dir.foreach(ARGV[0]) do |f|
  if f =~ /(.*)_c58(.*)/
    puts "git mv #{f} #{$1}_c56#{$2}"
  end
end

