import React, { useState, useEffect } from 'react';
import { preferencesAPI } from '../services/api';
import { Save, Trash2, Plus, AlertCircle, FileX } from 'lucide-react';

export default function PreferencesForm({ userId, onSaveSuccess }) {
  const [avoidItem, setAvoidItem] = useState('');
  const [avoidList, setAvoidList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const fetchPreferences = async () => {
      try {
        const response = await preferencesAPI.get(userId);
        if (response.data) {
          setAvoidList(response.data.avoidList || []);
        }
      } catch (err) {
        console.error('Failed to load avoid list:', err);
      } finally {
        setFetching(false);
      }
    };
    fetchPreferences();
  }, [userId]);

  const handleAddAvoid = (e) => {
    e.preventDefault();
    if (avoidItem.trim()) {
      // Split by comma or newlines to allow pasting list at once
      const items = avoidItem
        .split(/[,\n]/)
        .map(i => i.trim())
        .filter(i => i.length > 0 && !avoidList.includes(i));
      
      if (items.length > 0) {
        setAvoidList([...avoidList, ...items]);
        setAvoidItem('');
      }
    }
  };

  const handleRemoveAvoid = (indexToRemove) => {
    setAvoidList(avoidList.filter((_, idx) => idx !== indexToRemove));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    try {
      await preferencesAPI.save(userId, {
        domain: "",
        skills: "",
        teamSize: 4,
        duration: 4,
        avoidList,
      });
      setMessage('Avoid list saved successfully!');
      if (onSaveSuccess) onSaveSuccess();
    } catch (err) {
      setMessage('Error saving avoid list.');
    } finally {
      setLoading(false);
    }
  };

  if (fetching) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-2xl mx-auto">
      <div className="glass-panel rounded-2xl p-8 shadow-2xl relative">
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2 bg-indigo-500/10 rounded-lg text-indigo-400">
            <FileX size={22} />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-slate-100 font-sans tracking-tight">Project Avoid List</h2>
            <p className="text-slate-400 text-sm">Add topics or peer projects you want to filter out</p>
          </div>
        </div>

        {message && (
          <div className={`p-4 rounded-xl mb-6 text-sm border ${
            message.includes('successfully') 
              ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400' 
              : 'bg-red-500/10 border-red-500/20 text-red-400'
          }`}>
            {message}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Avoid List Input */}
          <div className="space-y-3">
            <label className="block text-slate-300 text-xs font-semibold uppercase tracking-wider">
              Add Projects to Avoid
            </label>
            <p className="text-slate-500 text-xs leading-relaxed">
              Type a single project and click Add, <strong>or paste a comma-separated list of topics</strong> (e.g. <i>Fake News Detection, Intrusion Detection, Blockchain Voting</i>) to add them all at once.
            </p>

            <div className="flex gap-2">
              <textarea
                value={avoidItem}
                onChange={(e) => setAvoidItem(e.target.value)}
                placeholder="Type one, or paste list: fake news, intrusion detection, blockchain voting..."
                rows={2}
                className="flex-1 bg-slate-950/60 border border-slate-800 focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 rounded-xl py-2.5 px-4 text-slate-100 placeholder-slate-700 outline-none transition-all resize-none text-sm"
              />
              <button
                type="button"
                onClick={handleAddAvoid}
                className="px-5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl flex items-center justify-center gap-1.5 transition-all cursor-pointer text-sm font-semibold self-end h-12"
              >
                <Plus size={16} />
                <span>Add</span>
              </button>
            </div>

            {avoidList.length > 0 && (
              <div className="space-y-3 mt-4">
                <div className="flex justify-between items-center text-xs text-slate-500">
                  <span>Avoid list topics ({avoidList.length})</span>
                  <button
                    type="button"
                    onClick={() => setAvoidList([])}
                    className="text-red-400 hover:text-red-300 font-medium hover:underline transition-colors"
                  >
                    Clear All
                  </button>
                </div>
                <div className="flex flex-wrap gap-2 max-h-48 overflow-y-auto p-3 rounded-xl border border-slate-900 bg-slate-950/20">
                  {avoidList.map((item, idx) => (
                    <div
                      key={idx}
                      className="flex items-center gap-1.5 bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 text-xs font-medium py-1.5 px-3 rounded-full"
                    >
                      <span>{item}</span>
                      <button
                        type="button"
                        onClick={() => handleRemoveAvoid(idx)}
                        className="text-indigo-400 hover:text-indigo-200 focus:outline-none transition-colors cursor-pointer"
                      >
                        <Trash2 size={12} />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="border-t border-slate-900 pt-6">
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-500 hover:to-purple-500 text-white font-semibold py-3 rounded-xl shadow-lg hover:shadow-indigo-500/20 transition-all flex items-center justify-center gap-2 cursor-pointer disabled:opacity-50"
            >
              {loading ? 'Saving Avoid List...' : (
                <>
                  <Save size={18} />
                  <span>Save Avoid List</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
