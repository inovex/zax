#!/usr/bin/perl

use strict;
use warnings;

use LWP::Simple;

######################
# config
my $pubkey = "pub-c-d3743263-0749-4b81-966e-8b0f9aa11681";
my $subkey = "sub-c-14f8a197-2cfc-11e0-b966-7321195d4657";
my $url = "http://pubsub.pubnub.com/publish/pub-$pubkey/sub-$subkey/0/zabbixmobile/0/";

# end config
######################

use Data::Dumper;
use URI::Escape;


open(my $log, ">>", "/tmp/pubnub.log");
flock $log, 2;


print $log Dumper(\@ARGV);

my @lines = split(/\n/, $ARGV[2]);

print $log Dumper(\@lines);

my %msg = ();
for my $l (@lines) {
	my ($key, $val) = split(/=/, $l, 2);
	$msg{$key} = $val;
}

print $log Dumper(\%msg);

#$url .= uri_escape("{\"triggerid\": " . $ARGV[1] . ", \"message\": \"" . $ARGV[2] . "\"}");
$url .= "{\"triggerid\": " . $ARGV[1] . ", \"message\": \"" . $msg{"message"} . "\",\"status\": \"" . $msg{"status"} . "\"}";

print $log "$url\n";
print $log "\n";
print $log get($url);
print $log "\n------------------------------------\n\n";

close($log);

