#!/usr/bin/env python

from plumbum import local,cli
import os
import sys

class OaiHarvestError(StandardError):
    def __init__(self, message, args=None):
        self.message = message
        self.args = args

class OaiHarvest:

    def __init__(self, oai="/app/oai-harvest-manager", base="/app/workdir", output="test", name="test1", verbose=False):
        self.verbose = verbose
        self.output = output
        self.name = name
        self.base = base
        
        self.find = local["find"]
        self.rsync = local["rsync"]
        self.harvester = local[os.path.join(oai, "run-harvester.sh")]
        self.workdir = os.path.join(base, "workdir", "%s-%s" % (output, name))
        self.logdir = os.path.join(base, "log", "%s-%s" % (output, name))
        self.confdir = os.path.join(oai, "resources")
        self.outputdir = os.path.join(base, "output", output)
        self.config_file = "config-%s-%s.xml" % (output, name)

        if self.verbose:
            self.print_to_stdout("Config:\n")
            self.print_to_stdout("\tconf dir: %s\n" % self.confdir)
            self.print_to_stdout("\tconf file: %s\n" % self.config_file)
            self.print_to_stdout("\tlog dir: %s:\n" % self.logdir)
            self.print_to_stdout("\twork dir: %s\n" % self.workdir)
            self.print_to_stdout("\toutput dir: %s\n" % self.outputdir)

    def run(self):
        self.print_to_stdout("Harvest run started, output=%s, name=%s.\n" % (self.output, self.name))
        self.print_to_stdout("\tInitializing.\n")
        self.initialize()
        self.print_to_stdout("\t\tDone\n")

        self.print_to_stdout("\tRunning harvester.\n")
        self.run_harvest()
        self.print_to_stdout("\t\tDone\n")

        self.print_to_stdout("\tDone\n")

        self.print_to_stdout("Merging harvest result to output.\n")
        self.merge("results/cmdi")
        self.merge("results/cmdi-1_1")
        self.merge("oai-pmh")
        self.print_to_stdout("\tDone\n")

    def initialize(self):
        """
        Make sure that:
        - all required directories exist (create if needed)
        - all required files exist (throw error if they don't exist)
        """
        #Ensure conf dir and file exist
        if not os.path.exists(self.confdir):
            raise OaiHarvestError("Config dir [%s] not found" % self.confdir)
        absolute_config_file = os.path.join(self.confdir, self.config_file)
        if not os.path.isfile(absolute_config_file):
            raise OaiHarvestError("Config file [%s] not found" % absolute_config_file)
        #Ensure work, output and log directories exist
        self.make_dir(self.base, self.workdir, self.outputdir, self.logdir)

    def run_harvest(self):
        """
        Run the harvester
        """
        local.env["LOG_DIR"] = self.logdir
        command = [
            "workdir=%s" % self.workdir,
            "overview-file=%s" % os.path.join(self.workdir, "overview.xml"),
            os.path.join(self.confdir, self.config_file)
        ]

        if self.verbose:
            self.print_to_stdout("\t\tHarvester command:\n")
            self.print_to_stdout("\t\t\t%s " % self.harvester)
            for i in command:
                self.print_to_stdout("%s " % i)
            self.print_to_stdout("\n")

        return self.harvester(command)

    def merge(self, dir):
        """
        Merge the harvest result from the output directory into the output directory and make sure the source
        directory is properly cleaned
        """
        search_directory=os.path.join(self.workdir, dir)

        if not os.path.isdir(search_directory):
            print "Directory doesn't exist: %s" % search_directory
            return None

        destination = search_directory.replace(self.workdir, self.outputdir)

        self.print_to_stdout("\tDirectory: %s -> %s\n" % (search_directory,destination)
        list = self.search(search_directory)
        for line in list:
            source = line.strip()
            self.print_to_stdout("\t\t%s\n" % source)
            self.do_rsync(source, destination)
            self.do_cleanup(source)

    def search(self, directory):
        """
        Search all leaf directories in the specified directory
        """
        result = self.find(directory, "-mindepth", "1", "-type", "d")
        return result.splitlines()

    def do_rsync(self, source, destination):
        """
        Rsync source to destination
        """
        self.make_dir(destination)
        return self.rsync("-ahrv", "-delete", source, destination)

    def do_cleanup(self, dir):
        """
        Perform cleanup actions
        """

    def make_dir(self, *dirs):
        """
        Make all directories, including subdirectories, if they don't exist
        """
        for dir in dirs:
            if not os.path.isdir(dir):
                os.makedirs(name=dir)

    def print_to_stdout(self, text):
        """
        Print a message to stdout
        """
        sys.stdout.write(text)
        sys.stdout.flush()

class App(cli.Application):
    PROGNAME = "oai-harvest.py"
    VERSION = "0.0.1"
    verbose = cli.Flag(["v", "verbose"], help="Verbose output")
    output = None
    name = None

    @cli.switch(["-o", "--output"], str, mandatory=True, help="Output folder (collection) this harvest is part of.")
    def set_output(self, output):
        self.output = output

    @cli.switch(["-n", "--name"], str, mandatory=True, help="Name for this harvest run.")
    def set_name(self, name):
        self.name = name

    def main(self):
        oai = OaiHarvest(output=self.output, name=self.name, verbose=self.verbose)
        try:
            oai.run()
        except Exception as e:
            print e.message
            sys.exit(1)
        else:
            sys.exit(0)


if __name__ == "__main__":
    App.run()
