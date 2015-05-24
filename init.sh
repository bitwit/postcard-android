#!/bin/sh

# Hold some packages before upgrading everything
echo grub-pc hold | dpkg --set-selections
echo grub-common hold | dpkg --set-selections
echo grub-pc-bin hold | dpkg --set-selections
echo grub2-common hold | dpkg --set-selections

#first update needed to get python software properties
sudo apt-get update

# Upgrade and install python essentials
sudo apt-get install -qq -y curl
sudo apt-get install -qq -y python-software-properties python g++ make
sudo apt-get install -qq -y build-essential
sudo apt-get install -qq -y git-core
sudo add-apt-repository -y ppa:chris-lea/node.js
#update again after adding private repository
sudo apt-get update
sudo apt-get install -qq -y nodejs

#prepare mongodb, get up to date
touch /etc/apt/sources.list.d/mongo.list
sudo sh -c 'echo "deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen" >> /etc/apt/sources.list.d/mongo.list'
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv 7F0CEB10
sudo apt-get update
sudo apt-get install -qq -y mongodb-10gen


#install coffee-script globally
sudo npm install -g coffee-script
#install nodemon watcher globally
sudo npm install -g nodemon

#move to working directory
cd /home/vagrant
#install any node dependencies for project
#npm install   #currently having problems, not needed at the moment

# Set up ZSH
sudo apt-get install -qq -y zsh
chsh -s /usr/bin/zsh vagrant
cd /home/vagrant
rm -Rf .oh-my-zsh oh-my-zsh zsh .zshrc .myzsh
git clone http://github.com/robbyrussell/oh-my-zsh.git
mv oh-my-zsh .oh-my-zsh
git clone http://github.com/abh1nav/myzsh.git
mv myzsh .myzsh
ln -s .myzsh/.zshrc
touch .localzshrc
cd /home/vagrant/.oh-my-zsh/themes
ln -s /home/vagrant/.myzsh/vibrantink.zsh-theme

# Set up VIM
sudo apt-get install -qq -y vim
cd /home/vagrant
rm -Rf .vim .vimrc
mkdir -p .vim/autoload .vim/bundle .vim/colors
curl -Sso .vim/autoload/pathogen.vim https://raw.github.com/tpope/vim-pathogen/master/autoload/pathogen.vim
wget https://raw.github.com/gist/4126831/6a6815df66766a5c922ddc8a4f72da2b2be0a7b5/.vimrc
cd .vim/colors
wget https://raw.github.com/jaromero/vim-monokai-refined/master/colors/Monokai-Refined.vim
cd ../bundle
git clone https://github.com/kchmck/vim-coffee-script

# Symlink in home directory (to be consistent with prod)
cd /home/vagrant
ln -s /vagrant nodetemplate

# Chown all the things
chown -R vagrant:vagrant /home/vagrant

