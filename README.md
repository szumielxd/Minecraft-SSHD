Minecraft-SSHD
===========

[![Build Status](https://travis-ci.org/Justasic/Minecraft-SSHD.svg?branch=master)](https://travis-ci.org/Justasic/Minecraft-SSHD)
[![Release](https://img.shields.io/github/release/Justasic/Minecraft-SSHD.svg?label=Release&maxAge=60)](https://github.com/Justasic/Minecraft-SSHD/releases/latest)
[![GitHub license](https://img.shields.io/github/license/Justasic/Minecraft-SSHD)](https://github.com/Justasic/Minecraft-SSHD/blob/master/LICENSE)

<img align="left" width="140" height="140" src="docs/ssh_logo.png?raw=true" hspace="5" vspace="5" alt="diskover"><br/>

**Have you ever wished you could remotely access your server's admin console without having to setup a complex remote access system? Now you can with Minecraft-SSHD!**

Minecraft-SSHD securely exposes your BungeeCord admin console and the server filesystem using the SSH protocol - the same protocol that serves as the secure foundation for nearly all remote server administration.<br/>

- Compatible with all ssh clients, regardless of operating system.
- Remotely view your server log in real-time.
- Remotely issue commands from the server console, just as if you were on the server itself.
- Supports multiple concurrent remote connections.
- Strong identity support using public key authentication.
- Audit history who is running commands in the console
- Run Spigot without using screen or tmux (by adding `-noconsole`)
- Remotely script your server by issuing one-off console commands with ssh.

### Why should I use Minecraft-SSHD?

- You are in a shared hosting environment that only gives you access to the - log files.
- You want to share access to your server console, but don't want to give anybody access to the machine its running on.
- You always wanted to use RCON, but want to see the server log as well.
- You are tired of running your server in a GNU screen or tmux session.
- You just want to access your server console using SSH.

Note: By default, only public key authentication is enabled. This is the most secure authentication mode! Setting a username and password will make your server less secure.

Screenshots
============

<img align="left" width="390" src="docs/console.png?raw=true" hspace="5" vspace="5" alt="console">
<img width="400" src="docs/session.png?raw=true" alt="session"><br>


Setting Up Public Key Authentication
====================================

Setting up public key authentication with SSH requires first generating a public and private key pair and then installing just the public key on your Spigot server. This plugin supports all modern SSH key algoritms as OpenSSH. You can paste as many public keys from the methods below into each user's authorization file if they have multiple private keys. You can read [this guide from ssh.com](https://www.ssh.com/ssh/keygen/) if you want a better explanation on different key files.

## Generating New Keys

#### On Windows

1. Ensure [Putty](https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html) is installed and open up `puttygen` (you can search for it in start search).
2. Click `Generate` and follow the directions.
3. When it finishes, set your key comment (if you like) and copy the text from the big `Public key for pasting into OpenSSH authorized_keys file`
4. Create a new file inside of the `plugins/SSHD/authorized_users` folder and name the file just the username (example: `justasic`, there should ***NOT*** be a file extension or authentication does not work).
5. Paste the key you copied from step 3 into the file you just created.
6. SSH into the server and see if your key works

#### On Linux/OS X

1. Open a terminal and run `ssh-keygen` then follow the prompts.
2. Copy the contents of your `id_<algorithm>.pub` file (example: if your key was generated with rsa, it will be named `id_rsa.pub`). This file is usually located in `/home/YOURUSERNAME/.ssh/`
3. Paste the contents of the .pub file into a new file inside the `plugins/SSHD/authorized_users` folder and name the file just the username that the user will use to login with (example: `justasic`, there should ***NOT*** be a file extension or authentication does not work).

## Using existing keys

#### On Windows

1. Ensure [Putty](https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html) is installed and open up `puttygen` (you can search for it in start search).
2. Click `Conversions` then click `Import Key` and select your .ppk file.
3. Copy the text from the big `Public key for pasting into OpenSSH authorized_keys file`
4. Create a new file inside of the `plugins/SSHD/authorized_users` folder and name the file just the username (example: `justasic`, there should ***NOT*** be a file extension or authentication does not work).
5. Paste the key you copied from step 3 into the file you just created.
6. SSH into the server and see if your key works

#### On Linux/OS X

1. Copy the contents of your `id_<algorithm>.pub` file (example: if your key was generated with rsa, it will be named `id_rsa.pub`). This file is usually located in `/home/YOURUSERNAME/.ssh/`
2. Paste the contents of the .pub file into a new file inside the `plugins/SSHD/authorized_users` folder and name the file just the username that the user will use to login with (example: `justasic`, there should ***NOT*** be a file extension or authentication does not work).

Plugin Usage
============

## Commands

    /mkpasswd <hash|help> <password>

mkpasswd supports the following hash algorithms:

- bcrypt - Using the OpenBSD-style Blowfish password hash
- sha256 - Using a basic salted sha256 hash
- pbkdf2 - Using the [PBKDF2](https://en.wikipedia.org/wiki/Pbkdf2) password hash
- PLAIN - Using plain text passwords (very insecure)

## Permissions

`sshd.mkpasswd` - Checks if the in-game user has access to run the mkpasswd command.

Minecraft-SSHD uses cryptographic certificates or a secure username and password to verify remote access.


## Source Code
[Get the source on GitHub](https://github.com/Justasic/Minecraft-SSHD "Source Code")
