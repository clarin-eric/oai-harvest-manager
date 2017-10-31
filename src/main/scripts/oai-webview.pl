
# No shebang here (due to Unicode).
# Run this as: perl -CSD oai-webview.pl


# -- OAI Harvester web view --

# This script generates an index page with links to all records
# harvested by the OAI Harvester during a particular harvesting
# cycle. The intended use is to have a periodic cron job perform
# re-harvesting and then to run this script to make the results
# visible. Since records do not change between harvests, it is not
# necessary to make this resource dynamic (i.e. creating a static html
# file is enough).

# This is intended as a way to show providers what has been harvested
# from them, not as an actual tool to navigate through metadata
# records.

# NOTE. This script expects that the file "oai2.xsl" is in the
# base directory.

# Lari Lampen / 2011-2014

# Copyright (C) 2000-2014, The Max Planck Institute for
# Psycholinguistics.



use strict;
use warnings;
use Sys::Hostname;


# used to keep track of the html tree
my @tags;

# Base directory and the hash that will hold records.
my ($basedir, $providers_hash);

# Keep count of things for statistical purposes (updated during
# building of the navi bar).
my %providercounts;
my %recordcounts;

# Location of providers.tab (OPTIONAL). If set, it is used to get the
# full proper names of the providers.
my $provider_list;
# This will contain proper provider names, if they exist.
my %pretty_names;


# These settings apply on server catalog.clarin.eu. For anywhere else,
# they may need to be edited.
$basedir='/lat/apache/htdocs/oai-harvester';
$provider_list='/lat/tools/oaiharvest/oai-harvester/resources/providers.tab';
$providers_hash = get_providers_hash("$basedir/clarin",
				     "$basedir/others");


# Read proper names of providers (if available).
read_provider_list();

# ---- create file for each provider
foreach my $prov (sort keys %$providers_hash) {
    h_openfile("$basedir/$prov.html");
    h_preamble();

    h_opentag('div', 'id="navi"');
    h_opentag('div', 'id="menu"');
    h_opentag('ul');
    h_opentag('li');
    h_opentag('ul');
    h_opentag('li');
    h_shorttag('a', "href=\"index.html\"", 'About this tool');
    h_closetag(4);
    print_navi($providers_hash, $prov);
    h_closetag(2);

    h_startbody();
    print_table($prov, $$providers_hash{$prov});
    h_closefile();
}

# Note that index.html is only created *after* all the provider pages
# have been finished, because it displays statistics collected during
# the creation of the provider pages.

# ---- write html index file, starting with preamble
h_openfile("$basedir/index.html");
h_preamble();

# ---- print list of providers (navigation bar)
h_opentag('div', 'id="navi"');
h_opentag('div', 'id="menu"');
h_opentag('ul');
h_opentag('li');
h_opentag('ul');
h_opentag('li');
h_shorttag('em', '', 'About this tool');
h_closetag(4);
print_navi($providers_hash);
h_closetag(2);

# ---- print the main content
h_startbody();
print_intro();

# ---- close remaining tags and file
h_closefile();


# End of main program.





# Read properly formatted names from $provider_list if it is
# defined. Otherwise do nothing.
sub read_provider_list {
    return unless defined $provider_list and -f $provider_list;
    my @orig_prov_names;
    open IF, $provider_list;
    while (my $a=<IF>) {
	next if ($a =~ /^#/);
	if ($a =~ /^[^\t]+\t([^\t]+)\t/) {
	    push @orig_prov_names, $1;
	}
    }
    close IF;
    foreach my $a (@orig_prov_names) {
	my $b=$a;
	# Don't use \W because it depends on encoding.
	$b =~ s/[^A-Za-z0-9]+/_/g;
	$pretty_names{$b} = $a;
    }
}


# ---- print introduction paragraph
sub print_intro {
    my $now = localtime time;
    h_shorttag('p', '', "This information was updated <strong>$now</strong>.");
    h_opentag('p');
    h_out('This simple web resource presents a view to the OAI records harvested by our OAI Harvester.');
    h_out('This is intended primarily as a techical tool ');
    h_out('to enable providers to check the status of harvested data. For actually viewing these metadata ');
    h_out('resources, there are more suitable alternatives, including the Virtual Language Observatory.');
    h_closetag();

    h_shorttag('p', '', 'The table below shows some statistics of the current state of the harvested data.');

    h_opentag('p');
    h_opentag('table');
    h_opentag('table', 'width="100%" border="0" cellspacing="0" cellpadding="2"');
    h_opentag('thead');
    h_opentag('tr');
    h_shorttag('th', 'align="left"', 'Type');
    h_shorttag('th', 'align="left"', 'Number of providers');
    h_shorttag('th', 'align="left"', 'Number of records');
    h_closetag(2);
    h_opentag('tbody');
    my ($provtot, $rectot, $errtot);
    foreach my $key (keys %recordcounts) {
	$recordcounts{$key} = 0 unless defined $recordcounts{$key};
	h_opentag('tr');
	h_shorttag('td', 'valign="top"', $key);
	h_shorttag('td', 'valign="top"', $providercounts{$key});
	h_shorttag('td', 'valign="top"', $recordcounts{$key});
	$provtot += $providercounts{$key};
	$rectot += $recordcounts{$key};
	h_closetag();
    }
    h_opentag('tr');
    h_opentag('td', 'valign="top"');
    h_shorttag('strong', '', 'TOTAL');
    h_closetag();
    h_opentag('td', 'valign="top"');
    h_shorttag('strong', '', $provtot);
    h_closetag();
    h_opentag('td', 'valign="top"');
    h_shorttag('strong', '', $rectot);
    h_closetag();
    h_closetag(5);

    h_shorttag('p', '', 'If you have any issues to report or suggestions to make, please contact '
	       .'<code>harvester at clarin.eu</code>.');
}

# Loop through all providers and print tables of records.
sub print_tables {
    my $providers = shift;

    foreach my $prov (sort keys %$providers) {
	my $provfiles=$$providers{$prov};
	print_table($prov, $provfiles);
    }
}

# Print table of records for one provider.
sub print_table {
    my $prov = shift;
    my $provfiles = shift;
    my @files=@{$provfiles};

    h_opentag('h1');
    h_shorttag('a', "name=\"$prov\"", mnice($prov).' ('.count($#files+1).')');
    h_closetag();

    if ($#files < 0) {
	h_opentag('p');
	h_out('Currently we have no records on file for this provider. This does ');
	h_out('not mean they provide none, but there may have been a connection or ');
	h_out('processing error at the time it was harvested. Consider checking this ');
	h_out('page again in a few days to see if the problem has been resolved. ');
	h_closetag();
	h_opentag('p');
	h_out('If the problem persists, please report it to ');
	h_out('<code>KeesJan.vandeLooij at mpi.nl</code>. ');
	h_out('Thank you!');
	h_closetag();
	return;
    }

    # NOTE. This just clumsily finds the timestamp of the oldest
    # file. In the future we should keep track of harvesting runs and
    # show a "schedule", not just a single date.
    my $oldtime=-1;
    foreach my $rec (@files) {
	# File modification time (seconds from epoch).
	my $file = $$rec{'file'};
	my $tt = (stat $file)[9];
	if ($oldtime == -1 || $oldtime>$tt) {
	    $oldtime = $tt;
	}
    }
    my $timestamp = localtime $oldtime;
    h_shorttag('p', '', "The harvested records were updated <strong>$timestamp</strong>.");

    h_shorttag('p', '', 'For each record, links are given to the extracted (or converted) metadata and'
	       .' the full OAI envelope that was received from the provider.');

    h_opentag('table', 'width="100%" border="0" cellspacing="0" cellpadding="2"');
    h_opentag('thead');
    h_opentag('tr');
    h_shorttag('th', 'align="left"', 'Record');
    h_shorttag('th', 'align="left"', 'Metadata content');
    h_shorttag('th', 'align="left"', 'OAI-PMH envelope');
    h_closetag(2);
    h_opentag('tbody');

    my $records = 0;
    my $clarin = 'other';
    foreach my $record (@files) {
	my $rec=$$record{'file'};
	my $rec_id=mshort($rec);
	$rec_id =~ s/^oai_//;
	$rec_id =~ s/\.xml$//;

	# It's a bit creepy to use .. in a directory path but Perl
	# allows this and it saves making another config variable.
#	my $xsl_loc = $$record{'src_dir'}.'/../oai2.xsl';
#	unless ( -e $xsl_loc) {
#	    symlink "$basedir/oai2.xsl", $xsl_loc;
#	}

	# CLARIN centres are distinguished from the rest by the hacky
	# method of string matching the file path. Ideally that
	# information would come from some configurable place instead.
	if ($$record{'file'} =~ /\/clarin\//) {
	    $clarin = 'CLARIN';
	}
	$records++;

	h_opentag('tr');
	if (length $rec_id > 40) {
	    h_shorttag('td', "valign=\"top\" title=\"$rec_id\"", substr($rec_id,0,38).'...');
	} else {
	    h_shorttag('td', 'valign="top"', $rec_id);
	}

	h_opentag('td', 'valign="top"');
	my @targfiles = @{$$record{'tgt_files'}};
	my @targformats = @{$$record{'tgt_formats'}};

	for (my $i=0; $i<=$#targfiles; $i++) {
	    h_out(" | ") if $i>0;
	    h_shorttag('a', 'href="'.relativize($targfiles[$i]).'"', clean_format($targformats[$i]));
	}

	h_closetag();
	h_opentag('td', 'valign="top"');
	# Link to original record in OAI envelope.
	h_shorttag('a', 'href="'.relativize($$record{'file'}).'"', 'XML');
	h_closetag(2);
    }


    $recordcounts{$clarin} += $records;
    $providercounts{$clarin}++;

    h_closetag(2);
}


sub relativize {
    my $file = shift;
    $file =~ s/$basedir\///;
    return $file;
}


sub clean_format {
    my $f=uc(shift);
    if ($f eq 'DC' || $f eq 'OAI_DC') {
	return "Dublin Core";
    }
    return $f;
}


# First parameter: provider hash. Second optional parameter: provider
# to omit link for (in directory format).
sub print_navi {
    my $providers = shift;
    my $omit = shift;
    h_opentag('ul');
    h_opentag('li');
    h_opentag('ul');
    foreach my $prov (sort keys %$providers) {
	my $provfiles=$$providers{$prov};
	my @files=@{$provfiles};

	h_opentag('li');
	unless (defined $omit && $prov eq $omit) {
	    h_shorttag('a', "href=\"$prov.html\"", mnice($prov).' ('.($#files+1).')');
	} else {
	    h_shorttag('em', '', mnice($prov).' ('.($#files+1).')');
	}
	h_closetag();
    }
    h_closetag(3);
}


# Get a hash with providers as keys and data structures as values that
# contain the source file location and arrays of converted/extracted
# file formats and locations.
sub get_providers_hash {
    my %providers;

    foreach my $sourcedir (@_) {
	my @oai_src=get_subdirs("$sourcedir/oai-pmh");
	my @targdirs=get_subdirs("$sourcedir/results");

	unless (@oai_src) {
	    print "WARNING: no contents in $sourcedir -- skipping";
	    next;
	}

	# ---- traverse oai-pmh envelope directory, collect list of providers
	# keys are provider names, values are arrays of target directories
	my %prov_hash;
	foreach my $dir (@oai_src) {
	    my $x = $dir;
	    $x =~ s/.*\///;
	    my @arr;
	    foreach my $targdir (@targdirs) {
		my $a = "$targdir/$x";
		push @arr, $a if -d $a;
	    }

	    $prov_hash{$x} = \@arr;
	}

	# ---- go through providers storing lists of records
	my ($prov,$target_dirs);
	while (($prov, $target_dirs) = each %prov_hash) {
	    my @files;
	    foreach my $file (<$sourcedir/oai-pmh/$prov/*>) {
		my @targfiles;
		my @targformats;
		foreach my $target_dir (@$target_dirs) {
		    my $f = "$target_dir/".strip_path($file);
		    if (-f $f) {
			push @targfiles, $f if -f $f;
			my $fmt = $target_dir;
			$fmt =~ s/\/[^\/]*$//;
			$fmt = strip_path($fmt);
			push @targformats, $fmt;
		    }
		}
		my $h={'tgt_formats'=> \@targformats,
		       'tgt_files' => \@targfiles,
		       'file' => $file};
		push @files, $h;
	    }
	    $providers{$prov}=\@files;
	}
    }

    return \%providers;
}

sub get_first_subdir {
    my $dir = shift;
    my @files = <$dir/*>;
    foreach my $f (@files) {
        if (-d $f) {
            return $f;
        }
    }
}

sub get_subdirs {
    my $dir = shift;
    my @files = <$dir/*>;
    my @res;
    foreach my $f (@files) {
        if (-d $f) {
	    push @res, $f;
        }
    }
    return @res;
}


# return file or directory name without path
sub strip_path {
    my $a=shift;
    $a =~ s/.*\///;
    return $a;
}


# ---- html printing subroutines ----

# h_opentag(TAG, ATTRIBS optional)
sub h_opentag {
    my $tag=shift;
    my $attrs=shift;
    if (!defined $attrs) {
	$attrs='';
    } elsif ($attrs ne '') {
	$attrs = ' '.$attrs;
    }
    h_out("<$tag$attrs>");
    push @tags, $tag;
}

sub h_closetag {
    my $num=shift;
    $num=1 unless defined $num;
    while ($num>0) {
	my $tag=pop @tags;
	h_out("</$tag>");
	$num--;
    }
}

# h_shorttag(TAG, ATTRIBS, CONTENT optional)
sub h_shorttag {
    my $tag=shift;
    my $attrs=shift;
    my $content=shift;
    $attrs = ' '.$attrs unless $attrs eq '';
    if (!defined $content) {
	h_out("<$tag$attrs/>");
    } else {
	h_out("<$tag$attrs>$content</$tag>");
    }
}

sub h_out {
    my $o=shift;
    my $ind=" " x (2*($#tags+1));
    print OF $ind, $o, "\n";
}

sub h_openfile {
    my $file=shift;
    open OF, ">$file";
    print OF '<?xml version="1.0" encoding="UTF-8"?>',"\n";
    print OF '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">',"\n";
}

# ---- write beginning part of the html file
sub h_preamble {
    h_opentag('html', 'xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"');
    h_opentag('head');
    h_shorttag('title', '', 'CLARIN OAI Harvester Web View');
    h_shorttag('meta', 'http-equiv="Content-Type" content="application/xhtml+xml; charset=UTF-8"', '');
    h_shorttag('link', 'rel="stylesheet" type="text/css" href="./style.css"');
    h_shorttag('link', 'rel="stylesheet" type="text/css" href="./navi.css"');
    h_closetag();

    # ---- beginning of body, top images
    h_opentag('body', 'class="background" bgcolor="#D4DEEF"');
    h_opentag('div', 'id="topleft"');
    h_opentag('a', 'onFocus="this.blur()" href="index.html"');
    h_shorttag('img', 'src="./topleft.gif" alt="" height="176" width="498" border="0"','');
    h_closetag();
    h_closetag();
    h_opentag('div', 'id="topright"');
    h_shorttag('img', 'src="./topright.gif" alt="" height="176" width="526" border="0"');
    h_closetag();
}

sub h_startbody {
    h_shorttag('div', 'id="locator"', 'CLARIN OAI Harvester');
    h_opentag('div', 'id="content"');
    h_opentag('h1');
    h_shorttag('a', 'name="intro"', 'CLARIN OAI Harvester Web View');
    h_closetag();
}

# close all remaining tags and then close file
sub h_closefile {
    while ($#tags>=0) {
	h_closetag();
    }
    close OF;
}


# ---- other subroutines ----

# cut off directory path, leaving file name
sub mshort {
    my $a=shift;
    $a =~ s/.*\///;
    return $a;
}

# If a proper name (read from providers.tab) exists, return
# it. Otherwise return provider name with underscores replaced by
# spaces.
sub mnice {
    my $a=shift;
    if (defined $provider_list && exists $pretty_names{$a}) {
	return $pretty_names{$a};
    }
    $a =~ s/_/ /g;
    return $a;
}

sub count {
    my $num=shift;
    if ($num == 1) {
	return "single record";
    } elsif ($num == 0) {
	return "no records";
    }
    return "$num records";
}
