#!/usr/bin/perl

use strict;
use warnings;

use LWP::Simple;
use LWP::UserAgent;

######################
# config
my $url = "http://localhost/push-server";

my $debug = 1;
my $logfile = "/var/log/zabbix/gcm.log";
my $csv = "/var/log/zabbix/gcm_push.csv"; # set to 0 to disable logging events to csv
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
#    print $log Dumper(\@ARGV);
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

my $json = "{\"triggerid\": " . $ARGV[1] . ", \"message\": \"" . $msg{"message"} . "\",\"status\": \"" . $msg{"status"} . "\"}";

if($debug){
    print $log "$url\n";
    print $log "$json\n";
#    print $log "\n------------------------------------\n\n";
}

my $ua = LWP::UserAgent->new;
my $req = HTTP::Request->new(POST => $url);
$req->header('content-type' => 'application/json');

$req->content($json);

my $response = $ua->request($req);
if ($response->is_success) {
    my $message = $response->decoded_content;
    print "Received reply: $message\n";
}
else {
    print $log "HTTP POST error code: ", $response->code, "\n";
    print $log "HTTP POST error message: ", $response->message, "\n";
}

close($log);

# DEBUG Logging each push-message into a csv-file
if($csv){
    open(my $pushlog, ">>", $csv);
    flock $pushlog,2;
    print $pushlog $datestring . "\t" . $ARGV[1] . "\t" . $msg{"status"} . "\t" . $msg{"message"} . "\n";
    close($pushlog);
}
