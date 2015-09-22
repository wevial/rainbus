# rainbow-table
*Originally created for the rainbow table project for CS4236 - Cryptography Theory at the National University of Singapore.*

RainBus is a rainbow table generator for 3 byte passwords.

What is a rainbow table?
---

Say you have a hash to a password and you'd like to crack it. One very inefficient approach is to do a brute-force attack which attempts to calculate a hash for every password. Another is to have a precomputed lookup table with hashes mapping to a password(s). A table that stored every possible hash and password, however, takes up a whole lot of space. A rainbow table is a practical alternative that takes up more space but less processing than brute-forcing and less space but more processing than a lookup table. It does this by precomputing "chains" of applying hash and a reduce functions on a plaintext many times (200x, 1000x, 100000x, etc) and arriving with a new "reduced" plaintext. These two values are stored in the table with the new plaintext being the key.

To lookup a password, 

Rainbow table attacks can be prevented with [salting] (https://en.wikipedia.org/wiki/Salt_(cryptography)).

[Check out Wikipedia's page on rainbow tables a more comprehensive explanation.] (https://en.wikipedia.org/wiki/Rainbow_table)

Project constraints
---
Password length: 3 bytes
Rainbow table size: ~500 KB
Accuracy: > 50%
Time: < 0.09 s


Updates
---
During my time at the Recurse Center, I cleaned up the code and made the rainbow tables generated both faster and more accurate.

