Spigot-SSHD
===========

[![Build Status](https://travis-ci.org/rmichela/Bukkit-SSHD.png)](https://travis-ci.org/rmichela/Bukkit-SSHD)

Have you ever wished you could remotely access your server's admin console without having to setup a complex remote access system? Now you can with SSHD.

SSHD securely exposes your Spigot admin console using the SSH protocol - the same protocol that serves as the secure foundation for nearly all remote server administration.

- Compatible with all ssh clients, regardless of operating system.
- Remotely view your server log in real-time.
- Remotely issue commands from the server console, just as if you were on the server itself.
- Supports multiple concurrent remote connections.
- Strong identity support using public key authentication.
- Remotely script your server by issuing one-off console commands with ssh.

## Why should I use SSHD?

- Your server runs on Windows.
- You are in a shared hosting environment that only gives you access to the - log files.
- You want to share access to your server console, but don't want to give anybody access to the machine its running on.
- You always wanted to use RCON, but want to see the server log as well.
- You are tired of running your server in a Screen session.
- You just want to access your server console using SSH.

## Configuration 

- **listenAddress** - The network interface(s) SSHD should listen on. (Default all)
- **port** - Specify the port SSHD should listen on. (Default 22)
- **username/password** - The credentials used to log into the server console. (Default blank)

Note: By default, only public key authentication is enabled. This is the most secure authentication mode! Setting a username and password will make your server less secure.

## Setting Up Public Key Authentication

Setting up public key authentication with SSH requires first generating a public and private key pair and then installing just the public key on your Spigot server.

On Windows

1. TODO

On Linux/OS X

1. TODO

## Commands
None - just install and go.

## Permissions

None - SSHD uses cryptographic certificates or a secure username and password to verify remote access.

## Source Code
[Get the source on GitHub](https://github.com/Justasic/Bukkit-SSHD "Source Code")

## Metrics

This plugin utilizes Hidendra's plugin metrics system. the following information is collected and sent to mcstats.org unless opted out:

- A unique identifier
- The server's version of Java
- Whether the server is in offline or online mode
- Plugin's version
- Server's version
- OS version/name and architecture
- core count for the CPU
- Number of players online
- Metrics version

Opting out of this service can be done by editing plugins/Plugin Metrics/config.yml and changing opt-out to true.
