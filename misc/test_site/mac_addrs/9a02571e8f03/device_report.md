# DAQ scan report for device 9a02571e8f03
Started %% 2019-05-29 16:44:59+00:00

Report generation error: 'device_info' is undefined
Failing data model:
{'modules': {'brute': {'enabled': True}, 'switch': {'enabled': False}, 'nmap': {'enabled': True}, 'tls': {'enabled': True}}, 'run_info': {'mac_addr': '9a:02:57:1e:8f:03', 'started': '2019-05-29T16:44:59.464Z', 'daq_version': '0.9.7', 'run_id': '5ceeb70b'}, 'device_description': 'N/A', 'process': {'approver': '*** Approver Name ***', 'reviewer': '*** Reviewer Name ***', 'operator': '*** Operator Name ***'}}

## Report summary

|Result|Test|Notes|
|---|---|---|
|skip|base.switch.ping||
|pass|base.target.ping|target |
|skip|network.brute||
|fail|security.ports.nmap||
|pass|security.tls.v3||
|pass|security.x509||

## Module ping

```
Baseline ping test report
%% 82 packets captured.
RESULT skip base.switch.ping
RESULT pass base.target.ping target %% 10.20.20.40
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
TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
Certificate is active for current date.
RESULT pass security.tls.v3
RESULT pass security.x509

Certificate:
[
[
  Version: V1
  Subject: CN=127.0.0.1, OU=Software, O=ExcelRedstone, L=KingsX, ST=London, C=GB
  Signature Algorithm: SHA256withRSA, OID = 1.2.840.113549.1.1.11

  Key:  Sun RSA public key, 2048 bits
  modulus: 23764005184914695362865097803100917542726025340138269848068574651328588247893816139889058768612205096365007754853867760587581490820433721355077530251866190239448726060679872650393451093376607205072475836040001629644656882306344509475418402870030191955690089436088822346211064692091784819293476466973033924518020454883755134420956631297713440401838415222282785310345158286296779095724800240565692141075483443934830068283767428378167231004486489446966039028329795376259126977893170057094102959074746941463588593029999722673580293078503932035665237349640093683868481587120065425510832233880863723314366450013992176435993
  public exponent: 65537
  Validity: [From: Wed May 29 15:22:01 GMT 2019,
               To: Thu May 28 15:22:01 GMT 2020]
  Issuer: CN=127.0.0.1, OU=Software, O=ExcelRedstone, L=KingsX, ST=London, C=GB
  SerialNumber: [    8324d21b 6376fe46]

]
  Algorithm: [SHA256withRSA]
  Signature:
0000: AC 91 A4 0F C3 85 F4 66   C8 D2 1E FF B7 DE 18 C1  .......f........
0010: 19 A5 35 5C 90 02 F8 0C   D9 97 E5 A8 85 3D 1C 08  ..5\.........=..
0020: 45 E4 53 40 2B 06 D0 C1   0B 46 D3 B3 8D 0B 02 F9  E.S@+....F......
0030: AD C4 2C FA 6D 80 99 CD   00 D7 E9 75 86 27 F7 96  ..,.m......u.'..
0040: 3A 9F D3 70 CA FB 09 BC   B3 5F 2C 77 1E E8 76 28  :..p....._,w..v(
0050: C2 B6 21 4F BE 2B BA 22   C5 D5 BB F3 B1 50 F5 C8  ..!O.+.".....P..
0060: C0 3F 24 83 10 2A 0A 73   54 13 59 C9 59 DC 8D 9C  .?$..*.sT.Y.Y...
0070: 79 99 1B B9 1B FD AA ED   4B 97 70 FE AC BC 45 86  y.......K.p...E.
0080: 82 9A 24 70 A2 1F 04 3B   64 98 20 6E 9D 34 78 42  ..$p...;d. n.4xB
0090: BB AB 98 6D D9 56 E2 82   19 29 95 25 0A 1D D8 33  ...m.V...).%...3
00A0: 25 0B CE 0C 88 0B 93 1B   78 AC D7 FA AD DD 48 E1  %.......x.....H.
00B0: 14 B0 D6 2D 3A A1 01 21   CC 43 DD AE 8D 96 30 50  ...-:..!.C....0P
00C0: 17 94 14 9C 6F 11 03 26   63 67 6F 67 B6 18 BD 77  ....o..&cgog...w
00D0: E9 F1 35 E5 C7 4F BF 4C   C0 B2 02 0F 49 F0 F0 A0  ..5..O.L....I...
00E0: 0C 7E 20 AC 75 FC 3D 80   3E D0 43 58 C1 44 FA 41  .. .u.=.>.CX.D.A
00F0: AE 63 4C 53 1A D8 75 78   14 93 C3 BA 39 37 C2 2E  .cLS..ux....97..

]
```

## Report complete

