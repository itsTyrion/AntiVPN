# AntiVPNVelocity
Blocks joining with VPN's, proxies, and from some hosters 

### Config explaination:

* **kickMessage**: The message players will be disconnected with when a VPN/Proxy is detected.
* **ipCacheDuration**: For how many hours will the plug-in cache wether an IP is considered *bad*.
* **logFailedAttempts**: If a connection is blocked, log the Player's name + IP?
* **preLogin**: Check the IP before the login request is completed. This is very lightweight but **DOES NOT** allow bypassing via permission.
* **bypassPermission**: If a player has this permission, they can join using a VPN or similar. Use with care. *preLogin* has to be **false**.
