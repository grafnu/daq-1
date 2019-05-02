# DAQ scan report for device 40bd32ebd57c
Started %% 2019-05-02 13:20:25+00:00

## Report summary

|Result|Test|Notes|
|---|---|---|
|pass|base.switch.ping|target 192.168.1.3:3
|
|pass|base.target.ping|target |
|skip|network.brute||
|fail|security.ports.nmap||
|pass|security.tls.v3||
|pass|security.x509||

## Module ping

```
Baseline ping test report
%% 205 packets captured.
RESULT pass base.switch.ping target 192.168.1.3:3
RESULT pass base.target.ping target %% 10.20.39.66
```

## Module nmap

```
Open port 443 open tcp https
RESULT fail security.ports.nmap
```

## Module brute

```
Target port 10000 not open.
RESULT skip network.brute
```

## Module tls

```
Cipher:
TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
Certificate is active for current date.
RESULT pass security.tls.v3
RESULT pass security.x509

Certificate:
[
[
  Version: V3
  Subject: CN=172.16.254.1, OU=Distech Controls Inc., O=Distech Controls Inc., L=Brossard, ST=Quebec, C=CA
  Signature Algorithm: SHA256withRSA, OID = 1.2.840.113549.1.1.11

  Key:  Sun RSA public key, 2048 bits
  modulus: 22193702051593733879651814095112568028627987214166787658232198799534435824628360144025387650178348767478806940276895827646543867954281786408767238355344489871010158396748543057762961654865641143557319483191940490442258728408322188461955698934730284707500443448473805360623267953127153373578293104607147073407428631326898259717391411030856735329502894605164403771160032234745953883118016483967205510752321641440657059910392780605793489692396749671250863410694921455228871624062570725070927276992228397903435451477150929134213470900054237769541230095243757991494257595147675707719779006918064592122713338351265861642103
  public exponent: 65537
  Validity: [From: Wed May 01 13:21:26 GMT 2019,
               To: Tue May 02 14:21:26 GMT 2119]
  Issuer: CN=Local ECLYPSE-EBD57C Authority, OU=Distech Controls Inc., O=Distech Controls Inc., L=Brossard, ST=Quebec, C=CA
  SerialNumber: [    016a78b4 f587]

Certificate Extensions: 3
[1]: ObjectId: 2.5.29.35 Criticality=false
AuthorityKeyIdentifier [
KeyIdentifier [
0000: 3C 3B B7 FB D3 8E 9A EB   8A DE 93 1B 3F 03 43 17  <;..........?.C.
0010: 1D F3 CA 6D                                        ...m
]
]

[2]: ObjectId: 2.5.29.17 Criticality=false
SubjectAlternativeName [
  DNSName: ECLYPSE-EBD57C
  IPAddress: 10.20.39.66
]

[3]: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: A8 5D 70 4E B6 19 16 09   46 C1 BD 78 7E 77 B2 B2  .]pN....F..x.w..
0010: 64 DD AD F7                                        d...
]
]

]
  Algorithm: [SHA256withRSA]
  Signature:
0000: A2 3A FE BA C9 2E FC 6A   48 17 7B 86 45 7E C0 4F  .:.....jH...E..O
0010: CE 2B E4 A0 64 41 5C 39   8F D9 3B B0 1B 4D 2D 9E  .+..dA\9..;..M-.
0020: 74 7D 09 E2 60 1B 61 5C   6C B3 3E 24 D2 34 91 48  t...`.a\l.>$.4.H
0030: FE 16 87 A9 D5 B5 3D DC   EC 53 9C 11 D2 9C D1 E7  ......=..S......
0040: E3 21 48 47 E6 F1 F0 58   8E A3 45 0F AF 19 12 9C  .!HG...X..E.....
0050: 07 DA B0 77 E9 D2 D8 AB   26 B9 32 9E 3E 50 63 FF  ...w....&.2.>Pc.
0060: 2D 45 1F 2B 96 E0 CA 2B   6D AB 08 94 13 4B 9E F7  -E.+...+m....K..
0070: DF E9 DD 18 52 E3 B7 7F   30 B9 31 91 83 FD 14 01  ....R...0.1.....
0080: CD C9 07 D0 B1 79 2F F3   11 17 13 36 4E 5B C4 D7  .....y/....6N[..
0090: 75 C7 64 D6 04 48 43 72   0D 4F 1B 6C 9B 34 67 9B  u.d..HCr.O.l.4g.
00A0: 31 73 BD 6B 97 E5 FD EE   C4 5B 25 6F FC 67 CA CE  1s.k.....[%o.g..
00B0: 33 DE 88 0A F9 D3 7F 63   27 D0 C3 3F BB B8 55 8A  3......c'..?..U.
00C0: D0 95 4E 7D 8B 9B 8B 02   28 51 67 35 90 83 C1 89  ..N.....(Qg5....
00D0: AD E5 C9 E7 C1 9C E9 56   04 1B A1 39 EA B2 FD 47  .......V...9...G
00E0: 67 F2 46 C4 65 92 5A 2A   59 CC FE 8C 72 FE 22 C0  g.F.e.Z*Y...r.".
00F0: 07 6F CB 4F FC 13 4C 33   A6 8A C1 9B C7 05 33 DE  .o.O..L3......3.

]
```

## Report complete

