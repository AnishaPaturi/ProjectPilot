import React, { useState, useEffect } from 'react';
import { recommendationsAPI } from '../services/api';
import { Sparkles, FileText, Download, ChevronDown, ChevronUp, Calendar, ArrowRight, Layers, Cpu, Award } from 'lucide-react';

export default function Dashboard({ userId }) {
  const [papers, setPapers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [error, setError] = useState('');
  const [expandedPaper, setExpandedPaper] = useState(null);
  const [activeTabs, setActiveTabs] = useState({}); // { paperId: 'roadmap' | 'modules' | 'details' }

  const fetchSavedRecommendations = async () => {
    try {
      const response = await recommendationsAPI.getSaved(userId);
      setPapers(response.data || []);
    } catch (err) {
      console.error('Failed to load saved papers:', err);
    } finally {
      setFetching(false);
    }
  };

  useEffect(() => {
    fetchSavedRecommendations();
  }, [userId]);

  const handleGenerate = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await recommendationsAPI.generate(userId);
      setPapers(response.data || []);
    } catch (err) {
      setError(err.response?.data || 'Failed to generate recommendations. Please ensure your preferences are saved first.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadPdf = (paperId) => {
    window.open(recommendationsAPI.downloadPdfUrl(paperId), '_blank');
  };

  const toggleExpand = (paperId) => {
    if (expandedPaper === paperId) {
      setExpandedPaper(null);
    } else {
      setExpandedPaper(paperId);
      if (!activeTabs[paperId]) {
        setActiveTabs({ ...activeTabs, [paperId]: 'roadmap' });
      }
    }
  };

  const handleTabChange = (paperId, tab) => {
    setActiveTabs({ ...activeTabs, [paperId]: tab });
  };

  if (fetching) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Upper Control Bar */}
      <div className="glass-panel rounded-2xl p-6 flex flex-col md:flex-row justify-between items-center gap-4 shadow-xl">
        <div>
          <h2 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Sparkles size={20} className="text-indigo-400" />
            IEEE Recommendations & Plan Generator
          </h2>
          <p className="text-slate-400 text-xs mt-1">
            Let AI search, verify, filter, score, and map your academic project
          </p>
        </div>
        <button
          onClick={handleGenerate}
          disabled={loading}
          className="w-full md:w-auto px-6 py-3.5 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-semibold rounded-xl shadow-lg hover:shadow-indigo-500/20 transition-all flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
        >
          {loading ? 'Running Multi-Agents...' : (
            <>
              <Sparkles size={18} />
              <span>Generate New Recommendations</span>
            </>
          )}
        </button>
      </div>

      {error && (
        <div className="bg-red-500/10 border border-red-500/30 text-red-400 p-4 rounded-xl text-sm">
          {error}
        </div>
      )}

      {/* Recommendations List */}
      {papers.length === 0 ? (
        <div className="glass-panel rounded-2xl p-16 text-center shadow-lg border border-slate-900/50">
          <div className="inline-flex p-4 bg-slate-900/60 text-slate-500 rounded-full mb-4">
            <FileText size={32} />
          </div>
          <h3 className="text-lg font-semibold text-slate-300">No Recommendations Yet</h3>
          <p className="text-slate-500 text-sm mt-1 max-w-sm mx-auto">
            Configure your preferences in Settings, then click "Generate New Recommendations" to start the agent pipeline.
          </p>
        </div>
      ) : (
        <div className="space-y-6">
          {papers.map((paper, index) => {
            let planObj = {};
            try {
              if (paper.implementationPlan) {
                planObj = JSON.parse(paper.implementationPlan);
              }
            } catch (err) {
              console.error('JSON parsing error:', err);
            }

            const modules = planObj.modules || [];
            const roadmap = planObj.roadmap || [];
            const techStack = planObj.techStack || 'N/A';
            const architecture = planObj.architecture || 'N/A';
            const noveltyAdditions = planObj.noveltyAdditions || 'N/A';
            const activeTab = activeTabs[paper.id] || 'roadmap';

            return (
              <div 
                key={paper.id} 
                className="glass-panel rounded-2xl overflow-hidden border border-slate-900 hover:border-slate-800 transition-all shadow-xl"
              >
                {/* Paper Header / Overview */}
                <div 
                  onClick={() => toggleExpand(paper.id)}
                  className="p-6 md:p-8 flex flex-col md:flex-row justify-between items-start md:items-center gap-6 cursor-pointer select-none bg-slate-950/20 hover:bg-slate-950/40 transition-colors"
                >
                  <div className="flex-1 space-y-2">
                    <div className="flex items-center gap-2.5 flex-wrap">
                      <span className="bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 text-xs font-bold px-2.5 py-1 rounded-md">
                        IEEE Journal #{index + 1}
                      </span>
                      <span className="text-slate-500 text-xs">
                        {paper.journal} ({paper.year})
                      </span>
                    </div>
                    <h3 className="text-xl font-bold text-slate-100 hover:text-indigo-400 transition-colors leading-snug">
                      {paper.title}
                    </h3>
                    <p className="text-slate-400 text-xs italic">
                      By {paper.authors}
                    </p>
                  </div>

                  <div className="flex items-center gap-4 self-stretch md:self-auto justify-between border-t border-slate-900 md:border-t-0 pt-4 md:pt-0">
                    <div className="text-right">
                      <p className="text-slate-500 text-[10px] font-semibold uppercase tracking-wider">Feasibility Score</p>
                      <div className="flex items-center gap-1 text-indigo-400 font-extrabold text-2xl mt-0.5">
                        <Award size={20} />
                        <span>{paper.score?.toFixed(1)}</span>
                      </div>
                    </div>
                    <div className="p-2 bg-slate-900 rounded-lg text-slate-400 hover:text-slate-200 transition-all">
                      {expandedPaper === paper.id ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
                    </div>
                  </div>
                </div>

                {/* Expanded Sections */}
                {expandedPaper === paper.id && (
                  <div className="border-t border-slate-900 p-6 md:p-8 bg-slate-950/30 space-y-6">
                    {/* Abstract */}
                    <div>
                      <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2">Research Abstract</h4>
                      <p className="text-slate-300 text-sm leading-relaxed bg-slate-950/50 p-4 rounded-xl border border-slate-900">
                        {paper.abstractText}
                      </p>
                      <div className="mt-2.5 flex items-center gap-2 text-xs">
                        <span className="text-slate-500">DOI:</span>
                        <a 
                          href={paper.link} 
                          target="_blank" 
                          rel="noopener noreferrer" 
                          className="text-indigo-400 hover:underline"
                        >
                          {paper.doi}
                        </a>
                      </div>
                    </div>

                    {/* Report Tab Container */}
                    <div className="border-t border-slate-900 pt-6">
                      <div className="flex justify-between items-center flex-wrap gap-4 mb-4">
                        {/* Tabs */}
                        <div className="flex bg-slate-950 p-1 rounded-xl border border-slate-900">
                          <button
                            onClick={() => handleTabChange(paper.id, 'roadmap')}
                            className={`px-4 py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                              activeTab === 'roadmap' ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-400 hover:text-slate-200'
                            }`}
                          >
                            Roadmap
                          </button>
                          <button
                            onClick={() => handleTabChange(paper.id, 'modules')}
                            className={`px-4 py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                              activeTab === 'modules' ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-400 hover:text-slate-200'
                            }`}
                          >
                            Modules
                          </button>
                          <button
                            onClick={() => handleTabChange(paper.id, 'details')}
                            className={`px-4 py-2 text-xs font-semibold rounded-lg transition-all cursor-pointer ${
                              activeTab === 'details' ? 'bg-indigo-600 text-white shadow-md' : 'text-slate-400 hover:text-slate-200'
                            }`}
                          >
                            Architecture & Tech
                          </button>
                        </div>

                        {/* PDF Download Button */}
                        <button
                          onClick={() => handleDownloadPdf(paper.id)}
                          className="px-4 py-2 bg-slate-800 hover:bg-slate-700 hover:text-white text-slate-300 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-all cursor-pointer"
                        >
                          <Download size={14} />
                          <span>Download Report PDF</span>
                        </button>
                      </div>

                      {/* Tab Contents */}
                      <div className="bg-slate-950/40 border border-slate-900 rounded-xl p-6">
                        {activeTab === 'roadmap' && (
                          <div className="space-y-4">
                            {roadmap.length > 0 ? (
                              roadmap.map((step, sIdx) => (
                                <div key={sIdx} className="flex gap-4 items-start">
                                  <div className="flex flex-col items-center">
                                    <div className="p-2 bg-indigo-500/10 text-indigo-300 rounded-lg text-xs font-bold w-24 text-center border border-indigo-500/20">
                                      {step.week}
                                    </div>
                                    {sIdx < roadmap.length - 1 && (
                                      <div className="w-[1px] h-10 bg-slate-800 my-1"></div>
                                    )}
                                  </div>
                                  <div className="flex-1 bg-slate-950/70 border border-slate-900 rounded-xl p-4 text-slate-300 text-sm">
                                    {step.tasks}
                                  </div>
                                </div>
                              ))
                            ) : (
                              <p className="text-slate-500 text-sm">Roadmap not available.</p>
                            )}
                          </div>
                        )}

                        {activeTab === 'modules' && (
                          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {modules.length > 0 ? (
                              modules.map((mod, mIdx) => (
                                <div key={mIdx} className="bg-slate-950/70 border border-slate-900 rounded-xl p-5 space-y-2">
                                  <h5 className="text-indigo-400 font-bold text-sm flex items-center gap-2">
                                    <Layers size={14} />
                                    {mod.name}
                                  </h5>
                                  <p className="text-slate-400 text-xs leading-relaxed">
                                    {mod.description}
                                  </p>
                                </div>
                              ))
                            ) : (
                              <p className="text-slate-500 text-sm">Modules not available.</p>
                            )}
                          </div>
                        )}

                        {activeTab === 'details' && (
                          <div className="space-y-6">
                            <div className="space-y-2">
                              <h5 className="text-xs font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                                <Cpu size={14} className="text-slate-500" />
                                Recommended Tech Stack
                              </h5>
                              <p className="text-slate-200 text-sm bg-slate-950/70 p-3 rounded-lg border border-slate-900">
                                {techStack}
                              </p>
                            </div>
                            <div className="space-y-2">
                              <h5 className="text-xs font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                                <Layers size={14} className="text-slate-500" />
                                System Architecture
                              </h5>
                              <p className="text-slate-300 text-sm leading-relaxed bg-slate-950/70 p-4 rounded-lg border border-slate-900">
                                {architecture}
                              </p>
                            </div>
                            <div className="space-y-2">
                              <h5 className="text-xs font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                                <Sparkles size={14} className="text-slate-500" />
                                Innovation / Novelty Additions (For Extra Marks)
                              </h5>
                              <p className="text-slate-300 text-sm leading-relaxed bg-indigo-500/5 p-4 rounded-lg border border-indigo-500/10 text-indigo-200">
                                {noveltyAdditions}
                              </p>
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
