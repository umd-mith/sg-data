#! /bin/perl

use XML::SAX;
use XML::SAX::Machines qw(Pipeline);
use utf8;

{
  package MySAXHandler;

  use base qw(XML::SAX::Base);
  use HTML::Entities;

  sub start_document {
    my $self = shift;

    $self->{my_text} = "";
    $self->SUPER::start_document(@_);
  }
  
  sub start_element {
    my($self, $el) = @_;

    if($self -> {my_text} ne "") {
      $content = $self->{my_text};
      $content =~ s{\p{Space}}{ }g;
      $content =~ s{↓}{&darr;}g;
      encode_entities($content);
      #utf8::encode($content);
      $self->SUPER::characters({Data => $content});
      $self->{my_text} = "";
    }
    $el->{Attributes} = {};
    if($el -> {Name} ne 'font') {
      $self->SUPER::start_element($el);
    }
  }

  sub end_element {
    my($self, $el) = @_;

    $content = "";
    if($self -> {my_text} =~ /^\p{Space}+$/) {
      $content = $self -> {my_text};
      $self -> {my_text} = "";
    }
    else {
      $self -> {my_text} =~ /^(.*\P{Space})\p{Space}*$/;
      $content = $1;
      $self -> {my_text} = substr($self->{my_text}, length($content));
    }
    if($content ne "") {
      $content =~ s{\p{Space}}{ }g;
      $content =~ s{↓}{&darr;}g;
      #utf8::encode($content);
      encode_entities($content);

      $self->SUPER::characters({Data => $content});
    }
    if($el->{Name} ne 'font') {
      $self -> SUPER::end_element($el);
    }
  }

  sub comment { }

  sub processing_instruction { }

  sub characters {
    my($self, $data) = @_;
    
    my $text = $data->{Data};
    #utf8::encode($text);
    $self -> {my_text} .= $text;
    $self -> SUPER::characters({Data=>""});
  }

}

my $output = "";

Pipeline(
  MySAXHandler => \$output
)
->parse_file(*STDIN);

$output =~ s{<i>\p{Space}+</i>}{ }g;
$output =~ s{<i></i>}{}g;
$output =~ s{&amp;}{&}g;
$output =~ s{&acirc;&#134;&#147;}{&darr;}g;

print $output;

