# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant::Config.run do |config|
  config.vm.define :nodetemplate do |conf|
    conf.vm.box = "ubuntu"
    conf.vm.box_url = "https://s3.amazonaws.com/twitsprout-static/vagrant/ubuntu.box"
    conf.vm.forward_port 3000, 3000
    conf.vm.forward_port 27017, 27017
    conf.vm.provision :shell, :path => "init.sh"
  end
  config.vm.customize do |vm|
    vm.memory_size = 1024
  end
  config.vm.network :bridged, :bridge => "en0: Wi-Fi (AirPort)"
end