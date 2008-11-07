require "rubygems"
require "mq"

EM.run {
    amq = MQ.new
    EM.add_periodic_timer(1) { amq.queue("noises").publish("moo") }
}


