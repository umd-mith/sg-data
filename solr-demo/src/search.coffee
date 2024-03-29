# # Search

# Ajax-solr Component

(($) ->

  # Result Widget
  AjaxSolr.ResultWidget = AjaxSolr.AbstractWidget.extend

    facets: [["hand_mws","Mary Shelley's hand"],["hand_pbs","Percy Shelley's hand"],["added", "added text"],["deleted","deleted text"]]

    beforeRequest: ->
      $(this.target).html($('<img/>').attr('src', 'images/ajax-loader.gif'))

    afterRequest: ->  
      self = this
      $(this.target).empty() 
      response = this.manager.response
      
      facets = $('#facets').html "<ul/>"
      for f in self.facets
        facets.append self.facetLinks f[1], self.facetHandler(self, f[0], self.q)

      t = $(this.target).append "<dl/>"

      if response.response.docs.length > 0
        t.append $("<strong/>").text("Found "+response.response.docs.length+" results")
        for doc in response.response.docs
          console.log(doc)
          # create an empty form with data to pass on (this will change when this bit gets integrated with the rest)

          t.append self.viewLink doc.id, self.viewHandler(self, doc, response.highlighting[doc.id].text)
          t.append "<dd>"+response.highlighting[doc.id].text+"</dd>"        
        
      else t.append "<dt>No results</dt><dd> </dd>"       

    facetLinks: (value, handler) ->
      return $('<li/>').append $('<a href="#"/>').text(value).click(handler)

    facetHandler: (that, facet_field, facet_value) ->
      ->
        that.manager.store.remove 'fq'
        that.manager.store.addByValue 'fq', facet_field + ':' + AjaxSolr.Parameter.escapeValue(facet_value)
        that.doRequest()
        false

    viewLink: (value, handler) ->
      return $('<dt/>').append $('<a href="#"/>').text(value).click(handler) 

    viewHandler: (that, doc, hl) ->
      ->
        f = $('<form action="http://localhost:8888/" method="post"/>')
        f.append($('<input type="text" name="id"/>').val(doc.id))
        f.append($('<input type="text" name="text"/>').val(doc.text))
        f.append($('<input type="text" name="hl"/>').val(hl))
        f.submit()

  solrSearch = (solr, term) ->

    Manager = new AjaxSolr.Manager
      solrUrl: solr
    Manager.init()

    Manager.store.addByValue 'q', 'text:'+term
    Manager.store.addByValue 'rows','999'
    Manager.store.addByValue 'fl','id, text'
    Manager.store.addByValue 'hl', 'true'
    Manager.store.addByValue 'hl.fl', 'text'
    Manager.store.addByValue 'hl.fragsize', '0'
    Manager.store.addByValue 'sort', 'id asc'
    Manager.doRequest()

    Manager.addWidget new AjaxSolr.ResultWidget
      id: 'result',
      q: term
      target: '#docs'

  # export search function
  window.solrSearch = solrSearch

) jQuery