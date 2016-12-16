---
title: Runbook
excerpt: "Running a production Codenvy system"
layout: docs
permalink: /docs/admin-guide/runbook/
---

This article provides some performance and security related guidance for installing Codenvy, these aren't rules because every install is slightly different. This doc outlines some of the steps we take in running the public Codenvy cloud.
Some items are specific to Azure, others are more general.

# Network Infrastructure Planning
We have 2 classes of instances:
1. Machine Nodes that runs user workspaces
2. Master Nodes used for running internal Codenvy services.

Because of the different purpose and security constraints we will group all instances in two subnets: One for Codenvy services and another for MachineNode instances. This will allow us to use subnet mask, not individual IPs when writing firewall rules for host’s iptables and Azure SecurityRules sets. VNET will use addresses 10.0.0.0/8 and two subnets 10.1.0.0/16 for Services and 10.2.0.0/16 for MachineNodes.

Azure CLI snippet for creating network and security rules for Azure:
```
# Create Security Groups
azure network nsg create ${ResourceGroup} ${SecurityGroupSubNet1} ${Location}
azure network nsg create ${ResourceGroup} ${SecurityGroupSubNet2} ${Location}

azure network nsg create ${ResourceGroup} ${SecurityGroupNIC1} ${Location}
azure network nsg create ${ResourceGroup} ${SecurityGroupNIC2} ${Location}
```
```
# Create SubNets
azure network vnet subnet create ${ResourceGroup} ${VNetName} ${SubNetName1} -a ${SubNetAddr1} -o ${SecurityGroupSubNet1}
azure network vnet subnet create ${ResourceGroup} ${VNetName} ${SubNetName2} -a ${SubNetAddr2} -o ${SecurityGroupSubNet2}
```

# Create NIC's
azure network nic create ${ResourceGroup} ${NICName1} -k ${SubNetName1} -m ${VNetName} -p ${IP1} -o ${SecurityGroupNIC1} -l ${Location}
azure network nic create ${ResourceGroup} ${NICName2} -k ${SubNetName2} -m ${VNetName} -p ${IP2} -o ${SecurityGroupNIC2} -l ${Location}
azure network nic create ${ResourceGroup} ${NICName3} -k ${SubNetName2} -m ${VNetName} -p ${IP3} -o ${SecurityGroupNIC2} -l ${Location}
```

```
# Populate Security Group Rules
CreateSecRules () {
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 22 -c Allow -y 1000 -r Inbound  ${ResourceGroup} ${SecurityGroupSubNet1} ssh-allow-sgs1
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 80 -c Allow -y 1100 -r Inbound  ${ResourceGroup} ${SecurityGroupSubNet1} http-allow-sgs1
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 389 -c Allow -y 1200 -r Inbound  ${ResourceGroup} ${SecurityGroupSubNet1} ldap-allow-sgs1
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 27017 -c Allow -y 1300 -r Inbound  ${ResourceGroup} ${SecurityGroupSubNet1} mongo-allow-sgs1
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 28017 -c Allow -y 1400 -r Inbound  ${ResourceGroup} ${SecurityGroupSubNet1} mongo2-allow-sgs1
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 7777 -c Allow -y 1500 -r Inbound  ${ResourceGroup} ${SecurityGroupSubNet1} zabbix-http-allow-sgs1
  
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 22 -c Allow -y 1000 -r Inbound  ${ResourceGroup} ${SecurityGroupSubNet2} ssh-allow-sgs2
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 80 -c Allow -y 1100 -r Inbound  ${ResourceGroup} ${SecurityGroupSubNet2} http-allow-sgs2
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 32768-65535 -c Allow -y 1200 -r Inbound  ${ResourceGroup} ${SecurityGroupSubNet2} machines-allow-sgs2

  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 22 -c Allow -y 1000 -r Inbound  ${ResourceGroup} ${SecurityGroupNIC1} ssh-allow-sgn
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 80 -c Allow -y 1100 -r Inbound  ${ResourceGroup} ${SecurityGroupNIC1} http-allow-sgn1
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 389 -c Allow -y 1200 -r Inbound  ${ResourceGroup} ${SecurityGroupNIC1} ldap-allow-sgn1
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 27017 -c Allow -y 1300 -r Inbound  ${ResourceGroup} ${SecurityGroupNIC1} mongo-allow-sgn1
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 28017 -c Allow -y 1400 -r Inbound  ${ResourceGroup} ${SecurityGroupNIC1} mongo2-allow-sgn1
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 7777 -c Allow -y 1600 -r Inbound  ${ResourceGroup} ${SecurityGroupNIC1} zabbix-http-allow-sgn1

  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 22 -c Allow -y 1000 -r Inbound  ${ResourceGroup} ${SecurityGroupNIC2} ssh-allow-sgn2
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 80 -c Allow -y 1100 -r Inbound  ${ResourceGroup} ${SecurityGroupNIC2} http-allow-sgn2
  azure network nsg rule create -p Tcp -f \\* -o \\* -e \\* -u 32768-65535 -c Allow -y 1200 -r Inbound  ${ResourceGroup} ${SecurityGroupNIC2} machines-allow-sgn2
  }
```

# Disk Setup Planning
Because we are using clouds for hosting (Azure) we are able to quickly scale disk space up. To utilise this we choose LVM and XFS.

First check if it is installed:
`# yum install lvm2 xfsprogs`

Azure doesn’t allow to use caching for OS disks, so we are forced to have some parts of the Linux OS that usually require OS disks to be on separate data disk/volumes. You need to attach these data disks with caching turned on for both read and write.

This is a list of directories that need to be mounted to data disks partitions/volumes. For maximum performance all filesystems should be local, however, there is minimal impact to putting `/home/codenvy/codenvy-data/che-machines-logs` and `/home/codenvy/codenvy-data/logs` on NFS.

For Master Node:
```
/var/log/journal
/var/lib/pgsql
/var/lib/docker
/var/lib/docker-distribution
/home/codenvy/codenvy-data/fs
/home/codenvy/codenvy-data/che-machines-logs
/home/codenvy/codenvy-data/logs
```
And for Machine Nodes:
```
/var/log/journal
/var/lib/docker
/home/codenvy/codenvy-data
```

## For Setting Up Disks on Master Nodes
After starting the instance inside the first subnet we need to attach 3 data disks:
- For databases and logs, initial size is about 300 GB.
- For main project storage/backups, initial size is about 1TB.
- For holding docker image snapshots, initial size is about 1 TB.

1. Unmount used ephemeral disk: `# umount /dev/sdb1`
2. Zeroing beginning of ephemeral disk: `# dd if=/dev/zero of=/dev/sdb`
3. Add ephemeral disk to LVM: `# pvcreate /dev/sdb`
4. Create volume group: `# vgcreate vg-docker /dev/sdb`
5. Create swap volume with size 4 GB: `#  lvcreate -L 4G -n swap vg-docker`
6. Create docker’s metadata volume with size 2 GB: `#  lvcreate -L 2G -n metadata vg-docker`
7. Create docker data volume using all remaining disk space: `#  lvcreate -l 100%FREE -n data vg-docker`
8. Make swap volume become swap: `#  mkswap /dev/vg-docker/swap`
9. Activate swap volume: `#  swapon /dev/vg-docker/swap`

Prepare data disk for databases and logs:
1. Add data disk to LVM: `# pvcreate /dev/sdc`
2. Create volume group: `# vgcreate vg-data /dev/sdc`
3. Create journal volume with size 10 GB: `#  lvcreate -L 10G -n journal vg-data`
4. Create logs volume with size 20 GB: `#  lvcreate -L 20G -n logs vg-data`
5. Create machine-logs volume with size 30 GB: `#  lvcreate -L 30G -n machine-logs vg-data`
6. Create ldap volume with size 10 GB: `#  lvcreate -L 10G -n ldap vg-data`  (Not needed after 5.0.0-M5)
7. Create mongo volume with size 20 GB: `#  lvcreate -L 20G -n mongo vg-data`  (Not needed after 5.0.0-M5)
8. Create pgsql volume with size 20 GB: `#  lvcreate -L 20G -n psql vg-data`
9. Create docker data volume using all remaining disk space: `#  lvcreate -l 100%FREE -n docker vg-docker`

Prepare data disk for FS:
1. Add data disk to LVM: `# pvcreate /dev/sdd`
2. Create volume group: `# vgcreate vg-fs /dev/sdd`
3. Create FS volume using all remaining disk space: `#  lvcreate -l 100%FREE -n fs vg-fs`

Prepare data disk for snapshots:
1. Add data disk to LVM: `# pvcreate /dev/sde`
2. Create volume group: `# vgcreate vg-ddistr /dev/sde`
3. Create FS volume using all remaining disk space: `#  lvcreate -l 100%FREE -n ddistr vg-ddistr`

Make XFS filesystem for:
1. journal: `# mkfs.xfs /dev/vg-data/journal`
2. logs: `# mkfs.xfs /dev/vg-data/logs`
3. machine-logs: `# mkfs.xfs /dev/vg-data/machine-logs`
4. ldap: `# mkfs.xfs /dev/vg-data/ldap`  (Not needed after 5.0.0-M5)
5. mongo: `# mkfs.xfs /dev/vg-data/mongo`  (Not needed after 5.0.0-M5)
6. pgsql: `# mkfs.xfs /dev/vg-data/pgsql`
7. docker: `# mkfs.xfs /dev/vg-data/docker`
8. fs: `# mkfs.xfs /dev/vg-fs/fs`
9. Snapshots: `# mkfs.xfs /dev/vg-ddistr/ddistr`

For /etc/fstab entries, pay attention to *nofail* option

```
#
# /etc/fstab
# Created by anaconda on Thu Mar 14 11:03:34 2013
#
# Accessible filesystems, by reference, are maintained under '/dev/disk'
# See man pages fstab(5), findfs(8), mount(8) and/or blkid(8) for more info
#
LABEL=rootfs            /           ext4    defaults,relatime  1   1
tmpfs                   /dev/shm                tmpfs   defaults        0 0
devpts                  /dev/pts                devpts  gid=5,mode=620  0 0
sysfs                   /sys                    sysfs   defaults        0 0
proc                    /proc                   proc    defaults        0 0
/dev/vg-docker/swap     swap                    swap    defaults,nofail        0 0
/dev/vg-data/fs         /home/codenvy/codenvy-data/fs   xfs     defaults,nofail         0 0
/dev/vg-ddist/ddist     /var/lib/docker-distribution    xfs     defaults,nofail         0 0
/dev/vg-docker/machinelogs      /home/codenvy/codenvy-data/che-machines-logs    xfs     defaults,nofail         0 0
```

- Activate all fstab entries: `# mount -a`
- Check if swap activated: `# free`
- Check if all volumes mounted and have correct sizes: `# df -h`

## For Setting Up Disks on MachineNodes 
After starting instance inside second subnet we need to attach 2 data disk for docker internals and logs, initial size is about 300 GB. For main workspace project storage, initial size is about 1TB

1. Unmount used ephemeral disk: `# umount /dev/sdb1`
2. Zeroing beginning of ephemeral disk: `# dd if=/dev/zero of=/dev/sdb`
3. Add ephemeral disk to LVM: `# pvcreate /dev/sdb`
4. Create volume group: `# vgcreate vg-docker /dev/sdb`
5. Create swap volume with size 4 GB: `#  lvcreate -L 4G -n swap vg-docker`
6. Create docker’s metadata volume with size 2 GB: `#  lvcreate -L 2G -n metadata vg-docker`
7. Create docker data volume using all remaining disk space: `#  lvcreate -l 100%FREE -n data vg-docker`
8. Make swap volume become swap: `#  mkswap /dev/vg-docker/swap`
9. Activate swap volume: `#  swapon /dev/vg-docker/swap`

Prepare data disk for docker and logs:
1. Add data disk to LVM: `# pvcreate /dev/sdc`
2. Create volume group: `# vgcreate vg-data /dev/sdc`
3. Create journal volume with size 10 GB: `#  lvcreate -L 10G -n journal vg-data`
4. Create docker data volume using all remaining disk space: `#  lvcreate -l 100%FREE -n docker vg-data`

Prepare data disk for FS:
1. Add data disk to LVM: `# pvcreate /dev/sdd`
2. Create volume group: `# vgcreate vg-fs /dev/sdd`
3. Create FS volume using all remaining disk space: `#  lvcreate -l 100%FREE -n fs vg-fs`

Make XFS filesystem for:
1. journal: `# mkfs.xfs /dev/vg-data/journal`
2. docker: `# mkfs.xfs /dev/vg-data/docker`
3. FS: `# mkfs.xfs /dev/vg-fs/fs`

Add /etc/fstab entries, pay attention to *nofail* option

```
#
# /etc/fstab
# Created by anaconda on Thu Mar 14 11:03:34 2013
#
# Accessible filesystems, by reference, are maintained under '/dev/disk'
# See man pages fstab(5), findfs(8), mount(8) and/or blkid(8) for more info
#
LABEL=rootfs            /           ext4    defaults,relatime  1   1
tmpfs                   /dev/shm                tmpfs   defaults        0 0
devpts                  /dev/pts                devpts  gid=5,mode=620  0 0
sysfs                   /sys                    sysfs   defaults        0 0
proc                    /proc                   proc    defaults        0 0
/dev/vg-docker/swap     swap                    swap    defaults,nofail        0 0
/dev/vg-data/fs         /home/codenvy/codenvy-data/fs   xfs     defaults,nofail         0 0
/dev/vg-ddist/ddist     /var/lib/docker-distribution    xfs     defaults,nofail         0 0
/dev/vg-docker/machinelogs      /home/codenvy/codenvy-data/che-machines-logs    xfs     defaults,nofail         0 0
```

- Activate all fstab entries: `# mount -a`
- Check if swap activated: `# free`
- Check if all volumes mounted and have correct sizes: `# df -h`

### Increasing Disk Space
If additional disk space is needed you can attach a new data disk (caches turned ON), then issue the following commands:

1. Create physical volume: `# pvcreate /dev/sdg`
2. Scan disks for changes, new disk should appear here:`lvmdiskscan\npvdisplay\npvscan`
3. Extend volume group: `# vgextend vg-data /dev/sdg`
4. Extend logical volume: `# lvextend -l +100%FREE /dev/vg-fs/fs`
5. Grow actual FS, it must be run on mounted volume, no need to stop codenvy or unmount FS: `# xfs_growfs /home/codenvy/codenvy-data/fs/`

### Detaching Disks
If you need to detach disks from one Azure instance and to reattach it to other Azure instance, you need to stop all services that may be using that volume:
```
service puppet stop\nservice crond stop\nservice codenvy stop
```

1. Unmount FS: `# umount /home/codenvy/codenvy-data/fs`
2. Deactivate volume group: `# vgchange -an vg-fs`
3. Detach data disk using azure web-interface or CLI from old instance
4. Reattach data disk using azure web-interface or CLI to new instance (turn ON caching)
5. Activate volume group: `# vgchange -ay vg-fs`
