#!/usr/bin/perl

use strict;
use warnings;

use LWP::Simple;

######################
# config
# add your own keys here:
my $pubkey = "";
my $subkey = "";
my $url = "https://pubsub.pubnub.com/publish/$pubkey/$subkey/0/zabbixmobile/0/";

my $debug = 1;
my $logfile = "/var/log/zabbix/pubnub.log";
my $csv = "/var/log/zabbix/pubnub_push.csv"; # set to 0 to disable logging events to csv
# end config
######################

use Data::Dumper;
use URI::Escape;


open(my $log, ">>", $logfile);
flock $log, 2;
my $datestring = localtime();


if($debug){
    print $log $datestring . "\n";
}

my @lines = split(/\n/, $ARGV[2]);


if($debug){
    #print $log Dumper(\@ARGV);
    print $log Dumper(\@lines);
}

my %msg = ();
for my $l (@lines) {
    my ($key, $val) = split(/=/, $l, 2);
    # the message from zabbix contains carriage returns, which caused problems in the url.
    # the following line removes any carriage returns.
    $val =~ s/\r|\n//g;
    $msg{$key} = $val;
}

#print $log Dumper(\%msg);

#$url .= uri_escape("{\"triggerid\": " . $ARGV[1] . ", \"message\": \"" . $ARGV[2] . "\"}");
$url .= "{\"triggerid\": " . $ARGV[1] . ", \"message\": \"" . $msg{"message"} . "\",\"status\": \"" . $msg{"status"} . "\"}";

my $response = get($url);
if($debug){
    print $log "$url\n";
    print $log $response;
    print $log "\n";
#    print $log "\n------------------------------------\n\n";
}

close($log);

if($csv){
    open(my $pushlog, ">>", $csvfile);
    flock $pushlog,2;
    print $pushlog $datestring . "\t" . $ARGV[1] . "\t" . $msg{"status"} . "\t" . $msg{"message"} . "\n";
    close($pushlog);
}
