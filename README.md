# AntiVPNVelocity
Blocks joining with VPN's, proxies, and from some hosters

Config explanation:
------

* **kickMessage**: The message players will see when disconnected for using a VPN/Proxy.
* **ipCacheDuration**: For how many hours will the plug-in cache whether an IP is considered *bad*.
* **logFailedAttempts**: If a connection is blocked, log the Player's name + IP?
* **preLogin**: Check the IP before the login request is completed. This is very lightweight but **DOES NOT** allow bypassing via permission.
* **bypassPermission**: If a player has this permission, they can join using a VPN or similar. Use with care. *preLogin* has to be **false**.



FAQ:
------

### Does VPN service x bypass?
It shouldn't, but I can't assure you. There are new anonymization services with more IPs each by the day.

### Why is preLogin more lightweight/Why can't it be used with the bypass permission?
When a player tries to log in, their username is being sent, but not their UUID, which has to be requested from Mojang.
This also implies that someone with ill intentions can't "give" your server a temporary request block ("rate limit", ~10 min) by spamming logins and aborting them.

Since no UUID is known before fully processing the login, the permission system used doesn't have any data for the player loaded, yet.
