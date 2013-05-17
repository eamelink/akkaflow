package net.eamelink.akkaflow.util

import java.io.StringReader

import scala.xml.Elem

import org.activiti.bpmn.converter.BpmnXMLConverter

import javax.xml.stream.XMLInputFactory

object ProcessParser {

  def parseProcess(process: Elem) = {
    val definitions = <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:activiti="http://activiti.org/bpmn" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" typeLanguage="http://www.w3.org/2001/XMLSchema" expressionLanguage="http://www.w3.org/1999/XPath" targetNamespace="http://www.activiti.org/test">
                        { process }
                      </definitions>
    val reader = new StringReader(definitions.toString)
    val factory = XMLInputFactory.newInstance()
    val streamReader = factory.createXMLStreamReader(reader);
    val converter = new BpmnXMLConverter
    converter.convertToBpmnModel(streamReader).getProcesses().get(0)
  }
}