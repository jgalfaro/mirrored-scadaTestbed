"""
test__offline

"""
from __future__ import absolute_import

from nose.tools import raises
from unittest import TestCase
import json

import plotly

fig = {
    'data': [
        plotly.graph_objs.Scatter(x=[1, 2, 3], y=[10, 20, 30])
    ],
    'layout': plotly.graph_objs.Layout(
        title='offline plot'
    )
}

PLOTLYJS = plotly.offline.offline.get_plotlyjs()

class PlotlyOfflineTestCase(TestCase):
    def setUp(self):
        pass

    def _read_html(self, file_url):
        """ Read and return the HTML contents from a file_url
        in the form e.g. file:///Users/chriddyp/Repos/plotly.py/plotly-temp.html
        """
        with open(file_url.replace('file://', '').replace(' ', '')) as f:
            return f.read()

    def test_default_plot_generates_expected_html(self):
        data_json = json.dumps(fig['data'], cls=plotly.utils.PlotlyJSONEncoder)
        layout_json = json.dumps(
            fig['layout'],
            cls=plotly.utils.PlotlyJSONEncoder)

        html = self._read_html(plotly.offline.plot(fig))

        # I don't really want to test the entire script output, so
        # instead just make sure a few of the parts are in here?
        self.assertTrue('Plotly.newPlot' in html) # plot command is in there
        self.assertTrue(data_json in html)        # data is in there
        self.assertTrue(layout_json in html)      # so is layout
        self.assertTrue(PLOTLYJS in html)         # and the source code
        # and it's an <html> doc
        self.assertTrue(html.startswith('<html>') and html.endswith('</html>'))

    def test_including_plotlyjs(self):
        html = self._read_html(plotly.offline.plot(fig, include_plotlyjs=False))
        self.assertTrue(PLOTLYJS not in html)

    def test_div_output(self):
        html = plotly.offline.plot(fig, output_type='div')

        self.assertTrue('<html>' not in html and '</html>' not in html)
        self.assertTrue(html.startswith('<div>') and html.endswith('</div>'))

    def test_autoresizing(self):
        resize_code_strings = [
            'window.addEventListener("resize", ',
            'Plotly.Plots.resize('
        ]
        # If width or height wasn't specified, then we add a window resizer
        html = self._read_html(plotly.offline.plot(fig))
        for resize_code_string in resize_code_strings:
            self.assertTrue(resize_code_string in html)

        # If width or height was specified, then we don't resize
        html = plotly.offline.plot({
            'data': fig['data'],
            'layout': {
                'width': 500, 'height': 500
            }
        })
        for resize_code_string in resize_code_strings:
            self.assertTrue(resize_code_string not in html)
